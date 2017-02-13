package com.yoloo.backend.comment;

import com.googlecode.objectify.Key;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;
import io.reactivex.Observable;
import java.util.List;
import java.util.Random;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class CommentShardService implements ShardService<Comment, CommentShard> {

  @Override
  public List<Key<CommentShard>> createShardKeys(Iterable<Key<Comment>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShardKeys)
        .toList()
        .blockingGet();
  }

  @Override
  public List<Key<CommentShard>> createShardKeys(Key<Comment> entityKey) {
    return Observable
        .range(1, CommentShard.SHARD_COUNT)
        .map(id -> CommentShard.createKey(entityKey, id))
        .toList()
        .blockingGet();
  }

  @Override
  public List<CommentShard> createShards(Iterable<Key<Comment>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShards)
        .toList()
        .blockingGet();
  }

  @Override
  public List<CommentShard> createShards(Key<Comment> entityKey) {
    return Observable
        .range(1, CommentShard.SHARD_COUNT)
        .map(shardId -> createShard(entityKey, shardId))
        .toList()
        .blockingGet();
  }

  @Override
  public CommentShard createShard(Key<Comment> entityKey, int shardNum) {
    return CommentShard.builder()
        .id(ShardUtil.generateShardId(entityKey, shardNum))
        .votes(0)
        .build();
  }

  @Override
  public Key<CommentShard> getRandomShardKey(Key<Comment> entityKey) {
    final int shardNum = new Random().nextInt(CommentShard.SHARD_COUNT - 1 + 1) + 1;
    return CommentShard.createKey(entityKey, shardNum);
  }
}
