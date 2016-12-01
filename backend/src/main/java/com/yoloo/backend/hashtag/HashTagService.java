package com.yoloo.backend.hashtag;

import com.google.common.base.Optional;

import com.googlecode.objectify.Key;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.util.StringUtil;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public class HashTagService {

    public HashTag createHashTag(String name, String language, String groupIds,
                                 HashTagShardService service) {
        Key<HashTag> hashTagKey = ofy().factory().allocateId(HashTag.class);

        return HashTag.builder()
                .id(hashTagKey.getId())
                .name(name)
                .language(language)
                .groupKeys(extractGroupKeys(groupIds))
                .shardKeys(service.createShardKeys(hashTagKey))
                .build();
    }

    public HashTagGroup createGroup(String name) {
        return HashTagGroup.builder()
                .name(name)
                .totalHashTagCount(0)
                .totalQuestionCount(0)
                .build();
    }

    public HashTag updateHashTag(HashTag hashTag, Optional<String> name) {
        if (name.isPresent()) {
            hashTag = hashTag.withName(name.get());
        }

        return hashTag;
    }

    public HashTagGroup updateGroup(HashTagGroup group, Optional<String> name) {
        if (name.isPresent()) {
            group = group.withName(name.get());
        }

        return group;
    }

    private Collection<HashTagCounterShard> test(Set<String> hashTagIds) {
        return Observable.fromIterable(hashTagIds)
                .map(new Function<String, Key<HashTag>>() {
                    @Override
                    public Key<HashTag> apply(String s) throws Exception {
                        return Key.create(s);
                    }
                })
                .map(new Function<Key<HashTag>, Key<HashTagCounterShard>>() {
                    @Override
                    public Key<HashTagCounterShard> apply(Key<HashTag> key) throws Exception {
                        final int shardNum = new Random().nextInt(HashTagCounterShard.SHARD_COUNT - 1 + 1) + 1;
                        return Key.create(HashTagCounterShard.class, ShardUtil.generateShardId(key, shardNum));
                    }
                })
                .toList()
                .map(new Function<List<Key<HashTagCounterShard>>, Collection<HashTagCounterShard>>() {
                    @Override
                    public Collection<HashTagCounterShard> apply(List<Key<HashTagCounterShard>> keys) throws Exception {
                        return ofy().load().keys(keys).values();
                    }
                })
                .blockingGet();

    }

    private List<HashTagCounterShard> test2(Set<String> hashTagIds) {
        return Observable.fromIterable(test(hashTagIds))
                .map(new Function<HashTagCounterShard, HashTagCounterShard>() {
                    @Override
                    public HashTagCounterShard apply(HashTagCounterShard shard) throws Exception {
                        return shard.withQuestions(shard.getQuestions() + 1);
                    }
                })
                .toList()
                .blockingGet();
    }

    private List<Key<HashTagGroup>> extractGroupKeys(String groupIds) {
        return Observable
                .fromIterable(StringUtil.splitToSet(groupIds, ","))
                .map(new Function<String, Key<HashTagGroup>>() {
                    @Override
                    public Key<HashTagGroup> apply(String s) throws Exception {
                        return Key.create(s);
                    }
                })
                .toList()
                .blockingGet();
    }
}
