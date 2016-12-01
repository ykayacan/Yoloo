package com.yoloo.backend.question;

import com.googlecode.objectify.Key;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;

import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "newInstance")
public class QuestionShardService implements ShardService<Question, QuestionCounterShard> {

    @Override
    public List<Key<QuestionCounterShard>> createShardKeys(Iterable<Key<Question>> keys) {
        return Observable
                .fromIterable(keys)
                .concatMapIterable(new Function<Key<Question>,
                        Iterable<Key<QuestionCounterShard>>>() {
                    @Override
                    public Iterable<Key<QuestionCounterShard>> apply(Key<Question> key)
                            throws Exception {
                        return createShardKeys(key);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public List<Key<QuestionCounterShard>> createShardKeys(final Key<Question> entityKey) {
        return Observable
                .range(1, QuestionCounterShard.SHARD_COUNT)
                .map(new Function<Integer, Key<QuestionCounterShard>>() {
                    @Override
                    public Key<QuestionCounterShard> apply(Integer id)
                            throws Exception {
                        return createShardKey(entityKey, id);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public List<Key<QuestionCounterShard>> getShardKeys(Iterable<Question> entities) {
        return Observable
                .fromIterable(entities)
                .concatMapIterable(new Function<Question, Iterable<Key<QuestionCounterShard>>>() {
                    @Override
                    public Iterable<Key<QuestionCounterShard>> apply(Question question)
                            throws Exception {
                        return getShardKeys(question);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public List<Key<QuestionCounterShard>> getShardKeys(Question entity) {
        return entity.getShardKeys();
    }

    @Override
    public Key<QuestionCounterShard> createShardKey(Key<Question> entityKey, int shardNum) {
        return Key.create(QuestionCounterShard.class,
                ShardUtil.generateShardId(entityKey, shardNum));
    }

    @Override
    public List<QuestionCounterShard> createShards(Iterable<Key<Question>> keys) {
        return Observable
                .fromIterable(keys)
                .concatMapIterable(new Function<Key<Question>, Iterable<QuestionCounterShard>>() {
                    @Override
                    public Iterable<QuestionCounterShard> apply(Key<Question> key)
                            throws Exception {
                        return createShards(key);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public List<QuestionCounterShard> createShards(final Key<Question> entityKey) {
        return Observable
                .range(1, QuestionCounterShard.SHARD_COUNT)
                .map(new Function<Integer, QuestionCounterShard>() {
                    @Override
                    public QuestionCounterShard apply(Integer shardId) throws Exception {
                        return createShard(entityKey, shardId);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public QuestionCounterShard createShard(Key<Question> entityKey, int shardNum) {
        return QuestionCounterShard.builder()
                .id(ShardUtil.generateShardId(entityKey, shardNum))
                .comments(0)
                .reports(0)
                .votes(0)
                .build();
    }

    @Override
    public Key<QuestionCounterShard> getRandomShardKey(Key<Question> entityKey) {
        final int shardNum = new Random().nextInt(QuestionCounterShard.SHARD_COUNT - 1 + 1) + 1;
        return createShardKey(entityKey, shardNum);
    }
}