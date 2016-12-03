package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.tag.TagUtil.extractGroupKeys;

@RequiredArgsConstructor(staticName = "create")
public class TagController {

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
     * @param name     the name
     * @param language the language
     * @param groupIds the group ids
     * @param user     the user
     * @return the hash tag
     */
    public Tag addTag(String name, String language, final String groupIds, User user) {
        List<Key<TagGroup>> groupKeys = extractGroupKeys(groupIds).blockingGet();

        return tagService.createTag(name, language, groupKeys)
                .map(new Function<Tag, Tag>() {
                    @Override
                    public Tag apply(Tag tag) throws Exception {
                        List<Ref<TagCounterShard>> shardRefs = tagShardService
                                .createShardRefs(tag.getKey())
                                .blockingGet();

                        List<TagCounterShard> shards =
                                tagShardService.createShards(tag.getKey());

                        tag = tag.withShardRefs(shardRefs);

                        ImmutableList<Object> saveList = ImmutableList.builder()
                                .add(tag)
                                .addAll(shards)
                                .build();

                        ofy().save().entities(saveList).now();

                        return tag;
                    }
                })
                .blockingGet();
    }

    /**
     * Update tag.
     *
     * @param websafeTagId the websafe hash tag id
     * @param name             the name
     * @param user             the user
     * @return the hash tag
     */
    public Tag updateTag(String websafeTagId, final Optional<String> name, User user) {
        return Single.just(websafeTagId)
                .flatMap(new Function<String, SingleSource<Tag>>() {
                    @Override
                    public SingleSource<Tag> apply(String s) throws Exception {
                        Key<Tag> tagKey = Key.create(s);

                        Tag tag = ofy().load().key(tagKey).now();

                        return tagService.updateTag(tag, name);
                    }
                })
                .doOnSuccess(new Consumer<Tag>() {
                    @Override
                    public void accept(Tag tag) throws Exception {
                        ofy().save().entity(tag).now();
                    }
                })
                .blockingGet();
    }

    /**
     * Delete tag.
     *
     * @param websafeTagId the websafe hash tag id
     * @param user             the user
     */
    public void deleteTag(String websafeTagId, User user) {
        final Key<Tag> tagKey = Key.create(websafeTagId);

        Tag tag = ofy().load().key(tagKey).now();

        Map<Key<TagGroup>, TagGroup> map = ofy().load().keys(tag.getGroupKeys());

        for (TagGroup group : map.values()) {
            map.put(group.getKey(), group.withTotalTagCount(group.getTotalTagCount() - 1));
        }

        List<Key<TagCounterShard>> shardKeys =
                tagShardService.createShardKeys(tagKey);

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
     * @param name  the name
     * @param limit the limit
     * @param user  the user
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

            return CollectionResponse.<Tag>builder()
                    .setItems(tagQuery.list())
                    .build();
        } else {
            tagQuery = tagQuery
                    .filter(Tag.FIELD_NAME + " >=", name)
                    .filter(Tag.FIELD_NAME + " <", name + "\ufffd");

            return CollectionResponse.<Tag>builder()
                    .setItems(tagQuery.list())
                    .build();
        }
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
                .doOnSuccess(new Consumer<TagGroup>() {
                    @Override
                    public void accept(TagGroup tagGroup) throws Exception {
                        ofy().save().entity(tagGroup).now();
                    }
                })
                .blockingGet();
    }

    /**
     * Update group.
     *
     * @param websafeGroupId the websafe group id
     * @param name           the name
     * @param user           the user
     * @return the hash tag group
     */
    public TagGroup updateGroup(String websafeGroupId, final Optional<String> name, User user) {
        return Single.just(websafeGroupId)
                .flatMap(new Function<String, SingleSource<TagGroup>>() {
                    @Override
                    public SingleSource<TagGroup> apply(String s) throws Exception {
                        final Key<TagGroup> groupKey = Key.create(s);
                        TagGroup group = ofy().load().key(groupKey).now();

                        return tagService.updateGroup(group, name);
                    }
                })
                .doOnSuccess(new Consumer<TagGroup>() {
                    @Override
                    public void accept(TagGroup tagGroup) throws Exception {
                        ofy().save().entity(tagGroup).now();
                    }
                })
                .blockingGet();
    }

    /**
     * Delete group.
     *
     * @param websafeGroupId the websafe group id
     * @param user           the user
     */
    public void deleteGroup(String websafeGroupId, User user) {
        final Key<TagGroup> groupKey = Key.create(websafeGroupId);

        List<Tag> tagEntities = ofy().load().type(Tag.class)
                .filter(Tag.FIELD_GROUP_KEYS + " =", groupKey)
                .list();

        tagEntities = Observable.fromIterable(tagEntities)
                .map(new Function<Tag, Tag>() {
                    @Override
                    public Tag apply(Tag tag) throws Exception {
                        List<Key<TagGroup>> groupKeys = tag.getGroupKeys();
                        groupKeys.remove(groupKey);

                        return tag.withGroupKeys(groupKeys);
                    }
                })
                .toList()
                .blockingGet();

        ofy().defer().save().entities(tagEntities);
        ofy().defer().delete().key(groupKey);
    }
}