package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
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
  public Tag addTag(String name, String language, final String groupIds, User user) {
    return tagService
        .createTag(name, language, groupIds)
        .map(tag -> {
          List<TagCounterShard> shards =
              tagShardService.createShards(tag.getKey());

          List<Ref<TagCounterShard>> shardRefs = ShardUtil.createRefs(shards)
              .toList().blockingGet();

          tag = tag.withShardRefs(shardRefs);

          ImmutableList<Object> saveList = ImmutableList.builder()
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
        .flatMap(s -> {
          Key<Tag> tagKey = Key.create(s);

          Tag tag = ofy().load().key(tagKey).now();

          return tagService.updateTag(tag, name);
        })
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

    Map<Key<TagGroup>, TagGroup> map = ofy().load().keys(tag.getGroupKeys());

    for (TagGroup group : map.values()) {
      map.put(group.getKey(), group.withTotalTagCount(group.getTotalTagCount() - 1));
    }

    List<Key<TagCounterShard>> shardKeys = tagShardService.createShardKeys(tagKey);

    ImmutableList<Key<?>> deleteList = ImmutableList.<Key<?>>builder()
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
   * @param limit the limit
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<Tag> list(String name, Optional<Integer> limit, User user) {
    name = name.toLowerCase().trim();

    Query<TagGroup> groupQuery = ofy().load().type(TagGroup.class)
        .filter(TagGroup.FIELD_NAME + " >=", name)
        .filter(TagGroup.FIELD_NAME + " <", name + "\ufffd")
        .limit(limit.or(DEFAULT_LIST_LIMIT));

    List<Key<TagGroup>> groupKeys = groupQuery.keys().list();

    Query<Tag> tagQuery = ofy().load().type(Tag.class);

    if (!groupKeys.isEmpty()) {
      for (Key<TagGroup> key : groupKeys) {
        tagQuery = tagQuery.filter(Tag.FIELD_GROUP_KEYS + " =", key);
      }
    } else {
      tagQuery = tagQuery
          .filter(Tag.FIELD_NAME + " >=", name)
          .filter(Tag.FIELD_NAME + " <", name + "\ufffd");
    }

    return CollectionResponse.<Tag>builder()
        .setItems(tagQuery.list())
        .build();
  }

  public CollectionResponse<Tag> recommeded(User user) {
    List<Tag> recommendedTags = ofy().load().type(Tag.class)
        .order("-" + Tag.FIELD_QUESTIONS).limit(12).list();

    return CollectionResponse.<Tag>builder()
        .setItems(recommendedTags)
        .build();
  }

  /**
   * Add group.
   *
   * @param name the name
   * @param user the user
   * @return the hash tag group
   */
  public TagGroup addGroup(String name, User user) {
    return tagService.createGroup(name)
        .doOnSuccess(tagGroup -> ofy().save().entity(tagGroup).now())
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
  public TagGroup updateGroup(String groupId, final Optional<String> name, User user) {
    return Single.just(groupId)
        .flatMap(s -> {
          final Key<TagGroup> groupKey = Key.create(s);
          TagGroup group = ofy().load().key(groupKey).now();

          return tagService.updateGroup(group, name);
        })
        .doOnSuccess(tagGroup -> ofy().save().entity(tagGroup).now())
        .blockingGet();
  }

  /**
   * Delete group.
   *
   * @param groupId the websafe group id
   * @param user the user
   */
  public void deleteGroup(String groupId, User user) {
    final Key<TagGroup> groupKey = Key.create(groupId);

    List<Tag> tagEntities = ofy().load().type(Tag.class)
        .filter(Tag.FIELD_GROUP_KEYS + " =", groupKey)
        .list();

    tagEntities = Observable.fromIterable(tagEntities)
        .map(tag -> {
          List<Key<TagGroup>> groupKeys = tag.getGroupKeys();
          groupKeys.remove(groupKey);

          return tag.withGroupKeys(groupKeys);
        })
        .toList()
        .blockingGet();

    ofy().defer().save().entities(tagEntities);
    ofy().defer().delete().key(groupKey);
  }
}