package com.yoloo.backend.comment;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.shard.Shardable;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class CommentShardService implements Shardable<CommentShard, Comment> {

  @Override
  public Map<Ref<CommentShard>, CommentShard> createShardMapWithRef(Iterable<Key<Comment>> keys) {
    return Observable
        .fromIterable(keys)
        .flatMap(this::createShardsFromPostKey)
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override
  public Map<Key<CommentShard>, CommentShard> createShardMapWithKey(Iterable<Key<Comment>> keys) {
    return Observable
        .fromIterable(keys)
        .flatMap(this::createShardsFromPostKey)
        .toMap(Key::create)
        .blockingGet();
  }

  @Override
  public Map<Ref<CommentShard>, CommentShard> createShardMapWithRef(Key<Comment> key) {
    return createShardsFromPostKey(key).toMap(Ref::create).blockingGet();
  }

  @Override
  public Map<Key<CommentShard>, CommentShard> createShardMapWithKey(Key<Comment> key) {
    return createShardsFromPostKey(key).toMap(Key::create).blockingGet();
  }

  @Override
  public Key<CommentShard> getRandomShardKey(Key<Comment> entityKey) {
    final int shardNum = new Random().nextInt(ShardConfig.COMMENT_SHARD_COUNTER) + 1;
    return CommentShard.createKey(entityKey, shardNum);
  }

  @Override
  public Observable<List<Comment>> mergeShards(Collection<? extends Comment> entities) {
    return Observable
        .fromIterable(entities)
        .flatMap(this::mergeShards)
        .toList(entities.size() == 0 ? 1 : entities.size())
        .toObservable();
  }

  @Override
  public Observable<Comment> mergeShards(Comment entity) {
    return Observable
        .fromIterable(entity.getShards())
        .cast(CommentShard.class)
        .reduce((s1, s2) -> s1.addValues(s2.getVotes()))
        .map(s -> entity.withVoteCount(s.getVotes()))
        .toObservable();
  }

  private Observable<CommentShard> createShardsFromPostKey(Key<Comment> postKey) {
    return Observable
        .range(1, ShardConfig.COMMENT_SHARD_COUNTER)
        .map(shardNum -> createShard(postKey, shardNum));
  }

  private CommentShard createShard(Key<Comment> postKey, Integer shardNum) {
    return CommentShard
        .builder()
        .id(ShardUtil.generateShardId(postKey, shardNum))
        .votes(0L)
        .build();
  }
}
