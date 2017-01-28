package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.shard.ShardUtil;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class TagController extends Controller {

  private static final Logger logger =
      Logger.getLogger(TagController.class.getName());

  /**
   * Maximum number of comments to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 5;

  @NonNull
  private TagService tagService;

  @NonNull
  private TagShardService tagShardService;

  /**
   * Add tag.
   *
   * @param name the name
   * @param language the language
   * @param groupIds the group ids
   * @param user the user
   * @return the hash tag
   */
  public Tag addTag(String name, String language, String groupIds, User user) {
    return tagService.createTag(name, language, groupIds)
        .map(tag -> {
          List<TagCounterShard> shards = tagShardService.createShards(tag.getKey());

          List<Ref<TagCounterShard>> shardRefs = ShardUtil.createRefs(shards)
              .toList().blockingGet();

          tag = tag.withShardRefs(shardRefs);

          ImmutableSet<Object> saveList = ImmutableSet.builder()
              .add(tag)
              .addAll(shards)
              .build();

          ofy().save().entities(saveList).now();

          return tag;
        })
        .blockingGet();
  }

  /**
   * Update tag.
   *
   * @param tagId the websafe hash tag id
   * @param name the name
   * @param user the user
   * @return the hash tag
   */
  public Tag updateTag(String tagId, final Optional<String> name, User user) {
    return Single.just(tagId)
        .map(Key::<Tag>create)
        .map(tagKey -> ofy().load().key(tagKey).now())
        .flatMap(tag -> tagService.updateTag(tag, name))
        .doOnSuccess(tag -> ofy().save().entity(tag).now())
        .blockingGet();
  }

  /**
   * Delete tag.
   *
   * @param tagId the websafe hash tag id
   * @param user the user
   */
  public void deleteTag(String tagId, User user) {
    final Key<Tag> tagKey = Key.create(tagId);

    Tag tag = ofy().load().key(tagKey).now();

    Map<Key<Tag>, Tag> map = ofy().load().keys(tag.getGroupKeys());

    for (Map.Entry<Key<Tag>, Tag> entry : map.entrySet()) {
      map.put(entry.getKey(),
          entry.getValue().withTotalTagCount(entry.getValue().getTotalTagCount() - 1));
    }

    List<Key<TagCounterShard>> shardKeys = tagShardService.createShardKeys(tagKey);

    ImmutableSet<Key<?>> deleteList = ImmutableSet.<Key<?>>builder()
        .add(tagKey)
        .addAll(shardKeys)
        .build();

    ofy().defer().delete().keys(deleteList);
    ofy().defer().save().entities(map.values());
  }

  /**
   * List collection response.
   *
   * @param name the name
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user   @return the collection response
   * @return the collection response
   */
  public CollectionResponse<Tag> list(String name, Optional<String> cursor, Optional<Integer> limit,
      User user) {
    name = name.toLowerCase().trim();

    Query<Tag> groupQuery;
    Query<Tag> tagQuery;

    tagQuery = groupQuery = ofy().load().type(Tag.class);

    groupQuery = groupQuery
        .filter(Tag.FIELD_NAME + " >=", name)
        .filter(Tag.FIELD_NAME + " <", name + "\ufffd")
        .filter(Tag.FIELD_TYPE + " =", Tag.Type.GROUP);

    List<Key<Tag>> groupKeys = groupQuery.keys().list();

    if (!groupKeys.isEmpty()) {
      for (Key<Tag> key : groupKeys) {
        tagQuery = tagQuery.filter(Tag.FIELD_GROUP_KEYS + " =", key);
      }
    } else {
      tagQuery = tagQuery
          .filter(Tag.FIELD_NAME + " >=", name)
          .filter(Tag.FIELD_NAME + " <", name + "\ufffd");
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
   * @param user the user
   * @return the list
   */
  public List<Tag> recommendedTags(User user) {
    return ofy().load().type(Tag.class).order("-" + Tag.FIELD_QUESTIONS).limit(12).list();
  }

  /**
   * Add group.
   *
   * @param name the name
   * @param user the user
   * @return the hash tag group
   */
  public Tag addGroup(String name, User user) {
    return tagService.createGroup(name)
        .map(tag -> {
          List<TagCounterShard> shards = tagShardService.createShards(tag.getKey());

          List<Ref<TagCounterShard>> shardRefs = ShardUtil.createRefs(shards)
              .toList().blockingGet();

          tag = tag.withShardRefs(shardRefs);

          ImmutableSet<Object> saveList = ImmutableSet.builder()
              .add(tag)
              .addAll(shards)
              .build();

          ofy().save().entities(saveList).now();

          return tag;
        })
        .blockingGet();
  }

  /**
   * Update group.
   *
   * @param groupId the websafe group id
   * @param name the name
   * @param user the user
   * @return the hash tag group
   */
  public Tag updateGroup(String groupId, final Optional<String> name, User user) {
    return Single.just(groupId)
        .map(Key::<Tag>create)
        .map(tagKey -> ofy().load().key(tagKey).now())
        .flatMap(tag -> tagService.updateGroup(tag, name))
        .doOnSuccess(tag -> ofy().save().entity(tag).now())
        .blockingGet();
  }

  /**
   * Delete group.
   *
   * @param groupId the websafe group id
   * @param user the user
   */
  public void deleteGroup(String groupId, User user) {
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
        .doOnSuccess(updated -> {
          ofy().defer().save().entities(updated);
          ofy().defer().delete().key(groupKey);
        })
        .blockingGet();
  }
}