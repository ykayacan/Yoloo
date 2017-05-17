package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.post.PostEntity;
import ix.Ix;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
@AllArgsConstructor(staticName = "create")
public class TagController extends Controller {

  /**
   * Maximum number of comments to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 5;

  private final TagShardService tagShardService;

  /**
   * Add tag.
   *
   * @param name the name
   * @return the hash tag
   */
  public Tag insertTag(String name) {
    //Map<Ref<TagShard>, TagShard> shardMap = tagShardService.createShardMapWithRef(tagKey);
    Key<Tag> tagKey = Tag.createKey(name);

    Tag tag = Tag.builder().id(tagKey.getName()).name(name).rank(0.0D)
        //.shardRefs(Lists.newArrayList(shardMap.keySet()))
        .build();

    ofy().transact(() -> ofy().save().entity(tag).now());

    return tag;
  }

  /**
   * Update tag.
   *
   * @param tagId the websafe hash tag id
   * @param name the name
   * @return the hash tag
   */
  public Tag updateTag(String tagId, Optional<String> name) {
    return Ix
        .just(ofy().load().key(Key.<Tag>create(tagId)).now())
        .map(tag -> name.isPresent() ? tag.withName(name.get()) : tag)
        .doOnNext(tag -> ofy().transact(() -> ofy().save().entity(tag).now()))
        .single();
  }

  /**
   * Delete tag.
   *
   * @param tagId the websafe hash tag id
   */
  public void deleteTag(String tagId) {
    final Key<Tag> tagKey = Key.create(tagId);

    Set<Key<TagShard>> shardKeys = tagShardService.createShardMapWithKey(tagKey).keySet();

    ImmutableSet<Key<?>> deleteList =
        ImmutableSet.<Key<?>>builder().add(tagKey).addAll(shardKeys).build();

    ofy().transact(() -> ofy().delete().keys(deleteList));
  }

  /**
   * List collection response.
   *
   * @param name the name
   * @param cursor the cursor
   * @param limit the limit
   * @return the collection response
   */
  public CollectionResponse<Tag> list(String name, Optional<String> cursor,
      Optional<Integer> limit) {
    name = name.toLowerCase().trim();

    Query<Tag> tagQuery = ofy()
        .load()
        .type(Tag.class)
        .filter(Tag.FIELD_NAME + " >=", name)
        .filter(Tag.FIELD_NAME + " <", name + "\ufffd");

    tagQuery =
        cursor.isPresent() ? tagQuery.startAt(Cursor.fromWebSafeString(cursor.get())) : tagQuery;

    tagQuery = tagQuery.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Tag> qi = tagQuery.iterator();

    List<Tag> tags = new ArrayList<>(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      tags.add(qi.next());
    }

    return CollectionResponse.<Tag>builder()
        .setItems(tags)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  public CollectionResponse<Tag> listUsedTags(String groupId, Optional<String> cursor,
      Optional<Integer> limit) {
    Query<PostEntity> postQuery = ofy()
        .load()
        .type(PostEntity.class)
        .filter(PostEntity.FIELD_GROUP_KEY + " =", Key.create(groupId));

    postQuery = cursor.isPresent()
        ? postQuery.startAt(Cursor.fromWebSafeString(cursor.get()))
        : postQuery;

    postQuery = postQuery.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<PostEntity> qi = postQuery.iterator();

    return Ix
        .just(qi)
        .filter(Iterator::hasNext)
        .map(Iterator::next)
        .map(PostEntity::getTags)
        .flatMap(Ix::from)
        .distinct()
        .map(Tag::createKey)
        .collectToList()
        .map(keys -> ofy().load().keys(keys).values())
        .map(tags -> CollectionResponse.<Tag>builder()
            .setItems(tags)
            .setNextPageToken(qi.getCursor().toWebSafeString())
            .build())
        .single();
  }

  /**
   * Recommended tags list.
   *
   * @return the list
   */
  public List<Tag> getRecommendedTags() {
    return ofy().load().type(Tag.class).order("-" + Tag.FIELD_RANK).limit(12).list();
  }
}