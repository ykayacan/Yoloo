package com.yoloo.backend.tag;

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

    public Tag createHashTag(String name, String language, String groupIds,
                             TagShardService service) {
        Key<Tag> key = ofy().factory().allocateId(Tag.class);

        return Tag.builder()
                .id(key.getId())
                .name(name)
                .language(language)
                .groupKeys(extractGroupKeys(groupIds))
                .shardRefs(service.createShardRefs(key).blockingGet())
                .build();
    }

    public TagGroup createGroup(String name) {
        return TagGroup.builder()
                .name(name)
                .totalTagCount(0)
                .totalQuestionCount(0)
                .build();
    }

    public Tag updateHashTag(Tag tag, Optional<String> name) {
        if (name.isPresent()) {
            tag = tag.withName(name.get());
        }

        return tag;
    }

    public TagGroup updateGroup(TagGroup group, Optional<String> name) {
        if (name.isPresent()) {
            group = group.withName(name.get());
        }

        return group;
    }

    private Collection<TagCounterShard> test(Set<String> hashTagIds) {
        return Observable.fromIterable(hashTagIds)
                .map(new Function<String, Key<Tag>>() {
                    @Override
                    public Key<Tag> apply(String s) throws Exception {
                        return Key.create(s);
                    }
                })
                .map(new Function<Key<Tag>, Key<TagCounterShard>>() {
                    @Override
                    public Key<TagCounterShard> apply(Key<Tag> key) throws Exception {
                        final int shardNum = new Random().nextInt(TagCounterShard.SHARD_COUNT - 1 + 1) + 1;
                        return Key.create(TagCounterShard.class, ShardUtil.generateShardId(key, shardNum));
                    }
                })
                .toList()
                .map(new Function<List<Key<TagCounterShard>>, Collection<TagCounterShard>>() {
                    @Override
                    public Collection<TagCounterShard> apply(List<Key<TagCounterShard>> keys) throws Exception {
                        return ofy().load().keys(keys).values();
                    }
                })
                .blockingGet();

    }

    private List<TagCounterShard> test2(Set<String> hashTagIds) {
        return Observable.fromIterable(test(hashTagIds))
                .map(new Function<TagCounterShard, TagCounterShard>() {
                    @Override
                    public TagCounterShard apply(TagCounterShard shard) throws Exception {
                        return shard.withQuestions(shard.getQuestions() + 1);
                    }
                })
                .toList()
                .blockingGet();
    }

    private List<Key<TagGroup>> extractGroupKeys(String groupIds) {
        return Observable
                .fromIterable(StringUtil.splitToSet(groupIds, ","))
                .map(new Function<String, Key<TagGroup>>() {
                    @Override
                    public Key<TagGroup> apply(String s) throws Exception {
                        return Key.create(s);
                    }
                })
                .toList()
                .blockingGet();
    }
}
