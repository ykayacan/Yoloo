package com.yoloo.backend.hashtag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public class HashTagController {

    private static final Logger logger =
            Logger.getLogger(HashTagController.class.getName());

    /**
     * Maximum number of comments to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 5;

    @NonNull
    private HashTagService hashTagService;

    @NonNull
    private HashTagShardService hashTagShardService;

    /**
     * Add hash tag hash tag.
     *
     * @param name     the name
     * @param language the language
     * @param groupIds the group ids
     * @param user     the user
     * @return the hash tag
     */
    public HashTag addHashTag(String name, String language, String groupIds, User user) {
        HashTag hashTag = hashTagService.createHashTag(name, language, groupIds, hashTagShardService);

        List<HashTagCounterShard> shards = hashTagShardService.createShards(hashTag.getKey());

        ImmutableList<Object> saveList = ImmutableList.builder()
                .add(hashTag)
                .addAll(shards)
                .build();

        ofy().save().entities(saveList).now();

        return hashTag;
    }

    /**
     * Update hash tag hash tag.
     *
     * @param websafeHashTagId the websafe hash tag id
     * @param name             the name
     * @param user             the user
     * @return the hash tag
     */
    public HashTag updateHashTag(String websafeHashTagId, Optional<String> name, User user) {
        final Key<HashTag> hashTagKey = Key.create(websafeHashTagId);

        HashTag hashTag = ofy().load().key(hashTagKey).now();

        hashTag = hashTagService.updateHashTag(hashTag, name);

        ofy().save().entity(hashTag).now();

        return hashTag;
    }

    /**
     * Delete hash tag.
     *
     * @param websafeHashTagId the websafe hash tag id
     * @param user             the user
     */
    public void deleteHashTag(String websafeHashTagId, User user) {
        final Key<HashTag> hashTagKey = Key.create(websafeHashTagId);

        HashTag hashTag = ofy().load().key(hashTagKey).now();

        Map<Key<HashTagGroup>, HashTagGroup> map = ofy().load().keys(hashTag.getGroupKeys());

        for (HashTagGroup group : map.values()) {
            map.put(group.getKey(), group.withTotalHashTagCount(group.getTotalHashTagCount() - 1));
        }

        List<Key<HashTagCounterShard>> shardKeys =
                hashTagShardService.createShardKeys(hashTagKey);

        ImmutableList<Key<?>> deleteList = ImmutableList.<Key<?>>builder()
                .add(hashTagKey)
                .addAll(shardKeys)
                .build();

        ofy().defer().delete().keys(deleteList);
        ofy().defer().save().entities(map.values());
    }

    /**
     * List collection response.
     *
     * @param name   the name
     * @param cursor the cursor
     * @param limit  the limit
     * @param user   the user
     * @return the collection response
     */
    public CollectionResponse<HashTag> list(String name,
                                            Optional<String> cursor,
                                            Optional<Integer> limit,
                                            User user) {
        name = name.toLowerCase().trim();

        Query<HashTagGroup> groupQuery = ofy().load().type(HashTagGroup.class)
                .filter(HashTagGroup.FIELD_NAME + " >=", name)
                .filter(HashTagGroup.FIELD_NAME + " <", name + "\ufffd")
                .limit(DEFAULT_LIST_LIMIT);

        List<Key<HashTagGroup>> groupKeys = groupQuery.keys().list();

        Query<HashTag> hashTagQuery = ofy().load().type(HashTag.class);

        if (!groupKeys.isEmpty()) {
            for (Key<HashTagGroup> key : groupKeys) {
                hashTagQuery = hashTagQuery.filter(HashTag.FIELD_GROUP_KEYS + " =", key);
            }

            return CollectionResponse.<HashTag>builder()
                    .setItems(hashTagQuery.list())
                    .build();
        } else {
            hashTagQuery = hashTagQuery.filter(HashTag.FIELD_NAME + " >=", name)
                    .filter(HashTag.FIELD_NAME + " <", name + "\ufffd");

            return CollectionResponse.<HashTag>builder()
                    .setItems(hashTagQuery.list())
                    .build();
        }
    }

    /**
     * Add group hash tag group.
     *
     * @param name the name
     * @param user the user
     * @return the hash tag group
     */
    public HashTagGroup addGroup(String name, User user) {
        HashTagGroup group = hashTagService.createGroup(name);

        ofy().save().entity(group).now();

        return group;
    }

    /**
     * Update group hash tag group.
     *
     * @param websafeGroupId the websafe group id
     * @param name           the name
     * @param user           the user
     * @return the hash tag group
     */
    public HashTagGroup updateGroup(String websafeGroupId, Optional<String> name,
                                    User user) {
        final Key<HashTagGroup> groupKey = Key.create(websafeGroupId);

        HashTagGroup group = ofy().load().key(groupKey).now();

        group = hashTagService.updateGroup(group, name);

        ofy().save().entity(group).now();

        return group;
    }

    /**
     * Delete group.
     *
     * @param websafeGroupId the websafe group id
     * @param user           the user
     */
    public void deleteGroup(String websafeGroupId, User user) {
        final Key<HashTagGroup> groupKey = Key.create(websafeGroupId);

        List<HashTag> hashTags = ofy().load().type(HashTag.class)
                .filter(HashTag.FIELD_GROUP_KEYS + " =", groupKey)
                .list();

        hashTags = Observable.fromIterable(hashTags)
                .map(new Function<HashTag, HashTag>() {
                    @Override
                    public HashTag apply(HashTag hashTag) throws Exception {
                        List<Key<HashTagGroup>> groupKeys = hashTag.getGroupKeys();
                        groupKeys.remove(groupKey);

                        return hashTag.withGroupKeys(groupKeys);
                    }
                })
                .toList()
                .blockingGet();

        ofy().defer().save().entities(hashTags);
        ofy().defer().delete().key(groupKey);
    }
}