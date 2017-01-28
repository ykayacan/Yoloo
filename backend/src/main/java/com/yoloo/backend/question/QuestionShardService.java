package com.yoloo.backend.question;

import com.googlecode.objectify.Key;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;
import io.reactivex.Observable;
import java.util.List;
import java.util.Random;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class QuestionShardService implements ShardService<Question, QuestionCounterShard> {

  @Override
  public List<Key<QuestionCounterShard>> createShardKeys(Iterable<Key<Question>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShardKeys)
        .toList()
        .blockingGet();
  }

  @Override
  public List<Key<QuestionCounterShard>> createShardKeys(Key<Question> entityKey) {
    return Observable
        .range(1, QuestionCounterShard.SHARD_COUNT)
        .map(id -> QuestionCounterShard.createKey(entityKey, id))
        .toList()
        .blockingGet();
  }

  @Override
  public List<QuestionCounterShard> createShards(Iterable<Key<Question>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShards)
        .toList()
        .blockingGet();
  }

  @Override
  public List<QuestionCounterShard> createShards(Key<Question> entityKey) {
    return Observable
        .range(1, QuestionCounterShard.SHARD_COUNT)
        .map(shardId -> createShard(entityKey, shardId))
        .toList()
        .blockingGet();
  }

  @Override
  public QuestionCounterShard createShard(Key<Question> entityKey, int shardNum) {
    return QuestionCounterShard.builder()
        .id(ShardUtil.generateShardId(entityKey, shardNum))
        .comments(0L)
        .votes(0L)
        .reports(0)
        .build();
  }

  @Override
  public Key<QuestionCounterShard> getRandomShardKey(Key<Question> entityKey) {
    final int shardNum = new Random().nextInt(QuestionCounterShard.SHARD_COUNT - 1 + 1) + 1;
    return QuestionCounterShard.createKey(entityKey, shardNum);
  }
}