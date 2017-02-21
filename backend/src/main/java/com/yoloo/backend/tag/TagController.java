package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.util.KeyUtil;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class TagController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(TagController.class.getName());

  /**
   * Maximum number of comments to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 5;

  private TagShardService tagShardService;

  /**
   * Add tag.
   *
   * @param name the name
   * @param langCode the language
   * @param groupIds the group ids
   * @return the hash tag
   */
  public Tag insertTag(String name, String langCode, String groupIds) {
    final Key<Tag> tagKey = factory().allocateId(Tag.class);

    Map<Ref<TagShard>, TagShard> shardMap = createTagShardMap(tagKey);

    Tag tag = Tag.builder()
        .id(tagKey.getId())
        .name(name)
        .langCode(langCode)
        .type(Tag.Type.NORMAL)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .groupKeys(KeyUtil.<Tag>extractKeysFromIds(groupIds, ",").blockingSingle())
        .build();

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(tag)
        .addAll(shardMap.values())
        .build();

    ofy().save().entities(saveList).now();

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
    return Single.just(ofy().load().key(Key.<Tag>create(tagId)).now())
        .map(tag -> name.isPresent() ? tag.withName(name.get()) : tag)
        .doOnSuccess(tag -> ofy().save().entity(tag).now())
        .blockingGet();
  }

  /**
   * Delete tag.
   *
   * @param tagId the websafe hash tag id
   */
  public void deleteTag(String tagId) {
    final Key<Tag> tagKey = Key.create(tagId);

    Tag tag = ofy().load().key(tagKey).now();

    Map<Key<Tag>, Tag> groupMap = ofy().load().keys(tag.getGroupKeys());

    for (Map.Entry<Key<Tag>, Tag> entry : groupMap.entrySet()) {
      groupMap.put(entry.getKey(),
          entry.getValue().withTotalTagCount(entry.getValue().getTotalTagCount() - 1));
    }

    List<Key<TagShard>> shardKeys = tagShardService.createShardKeys(tagKey);

    ImmutableSet<Key<?>> deleteList = ImmutableSet.<Key<?>>builder()
        .add(tagKey)
        .addAll(shardKeys)
        .build();

    ofy().defer().delete().keys(deleteList);
    ofy().defer().save().entities(groupMap.values());
  }

  /**
   * List collection response.
   *
   * @param name the name
   * @param cursor the cursor
   * @param limit the limit
   * @return the collection response
   */
  public CollectionResponse<Tag> list(String name, Optional<String> cursor, Optional<Integer> limit) {
    name = name.toLowerCase().trim();

    Query<Tag> groupQuery;
    Query<Tag> tagQuery;

    tagQuery = groupQuery = ofy().load().type(Tag.class);

    groupQuery = groupQuery
        .filter(Tag.FIELD_NAME + " >=", name)
        .filter(Tag.FIELD_NAME + " <", name + "\ufffd")
        .filter(Tag.FIELD_TYPE + " =", Tag.Type.GROUP);

    List<Key<Tag>> groupKeys = groupQuery.keys().list();

    if (groupKeys.isEmpty()) {
      tagQuery = tagQuery
          .filter(Tag.FIELD_NAME + " >=", name)
          .filter(Tag.FIELD_NAME + " <", name + "\ufffd");
    } else {
      for (Key<Tag> key : groupKeys) {
        tagQuery = tagQuery.filter(Tag.FIELD_GROUP_KEYS + " =", key);
      }
    }

    // Fetch items from beginning from cursor.
    tagQuery = cursor.isPresent()
        ? tagQuery.startAt(Cursor.fromWebSafeString(cursor.get()))
        : tagQuery;

    // Limit items.
    tagQuery = tagQuery.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Tag> qi = tagQuery.iterator();

    List<Tag> questions = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      questions.add(qi.next());
    }

    return CollectionResponse.<Tag>builder()
        .setItems(tagQuery.list())
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  /**
   * Recommended tags list.
   *
   * @return the list
   */
  public List<Tag> recommendedTags() {
    return ofy().load().type(Tag.class).order("-" + Tag.FIELD_QUESTIONS).limit(12).list();
  }

  /**
   * Add group.
   *
   * @param name the name
   * @return the hash tag group
   */
  public Tag insertGroup(String name) {
    final Key<Tag> tagKey = factory().allocateId(Tag.class);

    Map<Ref<TagShard>, TagShard> shardMap = createTagShardMap(tagKey);

    Tag tag = Tag.builder()
        .id(tagKey.getId())
        .name(name)
        .type(Tag.Type.GROUP)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .totalTagCount(0L)
        .posts(0L)
        .build();

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(tag)
        .addAll(shardMap.values())
        .build();

    ofy().save().entities(saveList).now();

    return tag;
  }

  /**
   * Update group.
   *
   * @param groupId the websafe group id
   * @param name the name
   * @return the hash tag group
   */
  public Tag updateGroup(String groupId, final Optional<String> name) {
    return Single.just(ofy().load().key(Key.<Tag>create(groupId)).now())
        .map(tag -> name.isPresent() ? tag.withName(name.get()) : tag)
        .doOnSuccess(tag -> ofy().save().entity(tag).now())
        .blockingGet();
  }

  /**
   * Delete group.
   *
   * @param groupId the websafe group id
   */
  public void deleteGroup(String groupId) {
    final Key<Tag> groupKey = Key.create(groupId);

    List<Tag> tags = ofy().load().type(Tag.class)
        .filter(Tag.FIELD_GROUP_KEYS + " =", groupKey)
        .list();

    Observable.fromIterable(tags)
        .map(tag -> {
          List<Key<Tag>> groupKeys = tag.getGroupKeys();
          groupKeys.remove(groupKey);

          return tag.withGroupKeys(groupKeys);
        })
        .toList()
        .doOnSuccess(saveList -> {
          ofy().defer().save().entities(saveList);
          ofy().defer().delete().key(groupKey);
        })
        .subscribe();
  }

  private Map<Ref<TagShard>, TagShard> createTagShardMap(Key<Tag> tagKey) {
    return Observable.range(1, TagShard.SHARD_COUNT)
        .map(shardNum -> tagShardService.createShard(tagKey, shardNum))
        .toMap(Ref::create)
        .blockingGet();
  }
}