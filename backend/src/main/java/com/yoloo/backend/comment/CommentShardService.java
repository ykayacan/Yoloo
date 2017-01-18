package com.yoloo.backend.comment;

import com.googlecode.objectify.Key;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;
import io.reactivex.Observable;
import java.util.List;
import java.util.Random;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class CommentShardService implements ShardService<Comment, CommentCounterShard> {

  @Override
  public List<Key<CommentCounterShard>> createShardKeys(Iterable<Key<Comment>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShardKeys)
        .toList()
        .blockingGet();
  }

  @Override
  public List<Key<CommentCounterShard>> createShardKeys(Key<Comment> entityKey) {
    return Observable
        .range(1, CommentCounterShard.SHARD_COUNT)
        .map(id -> CommentCounterShard.createKey(entityKey, id))
        .toList()
        .blockingGet();
  }

  @Override
  public List<CommentCounterShard> createShards(Iterable<Key<Comment>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShards)
        .toList()
        .blockingGet();
  }

  @Override
  public List<CommentCounterShard> createShards(Key<Comment> entityKey) {
    return Observable
        .range(1, CommentCounterShard.SHARD_COUNT)
        .map(shardId -> createShard(entityKey, shardId))
        .toList()
        .blockingGet();
  }

  @Override
  public CommentCounterShard createShard(Key<Comment> entityKey, int shardNum) {
    return CommentCounterShard.builder()
        .id(ShardUtil.generateShardId(entityKey, shardNum))
        .votes(0)
        .build();
  }

  @Override
  public Key<CommentCounterShard> getRandomShardKey(Key<Comment> entityKey) {
    final int shardNum = new Random().nextInt(CommentCounterShard.SHARD_COUNT - 1 + 1) + 1;
    return CommentCounterShard.createKey(entityKey, shardNum);
  }
}
