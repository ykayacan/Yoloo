package com.yoloo.backend.post;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.bookmark.Bookmark;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.shard.Shardable;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(staticName = "create")
public class PostShardService implements Shardable<PostEntity.PostShard, PostEntity> {

  @Override
  public Map<Ref<PostEntity.PostShard>, PostEntity.PostShard> createShardMapWithRef(
      Iterable<Key<PostEntity>> keys) {
    return Observable
        .fromIterable(keys)
        .flatMap(this::createShardsFromPostKey)
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override
  public Map<Key<PostEntity.PostShard>, PostEntity.PostShard> createShardMapWithKey(
      Iterable<Key<PostEntity>> keys) {
    return Observable
        .fromIterable(keys)
        .flatMap(this::createShardsFromPostKey)
        .toMap(Key::create)
        .blockingGet();
  }

  @Override
  public Map<Ref<PostEntity.PostShard>, PostEntity.PostShard> createShardMapWithRef(
      Key<PostEntity> key) {
    return createShardsFromPostKey(key).toMap(Ref::create).blockingGet();
  }

  @Override
  public Map<Key<PostEntity.PostShard>, PostEntity.PostShard> createShardMapWithKey(
      Key<PostEntity> key) {
    return createShardsFromPostKey(key).toMap(Key::create).blockingGet();
  }

  @Override
  public Key<PostEntity.PostShard> getRandomShardKey(Key<PostEntity> entityKey) {
    final int shardNum = new Random().nextInt(ShardConfig.POST_SHARD_COUNTER - 1 + 1) + 1;
    return PostEntity.PostShard.createKey(entityKey, shardNum);
  }

  @Override
  public Observable<List<PostEntity>> mergeShards(Collection<? extends PostEntity> entities) {
    return Observable
        .fromIterable(entities)
        .flatMap(this::mergeShards)
        .toList(entities.size() == 0 ? 1 : entities.size())
        .toObservable();
  }

  public Observable<List<PostEntity>> mergeShards(Collection<? extends PostEntity> entities,
      Key<Account> accountKey) {
    return Observable
        .fromIterable(entities)
        .flatMap(o -> mergeShards(o, accountKey))
        .toList(entities.size() == 0 ? 1 : entities.size())
        .toObservable();
  }

  @Override
  public Observable<PostEntity> mergeShards(PostEntity entity) {
    return Observable
        .fromIterable(entity.getShards())
        .cast(PostEntity.PostShard.class)
        .reduce(PostEntity.PostShard::mergeWith)
        .map(s -> entity.withVoteCount(s.getVoteCount()).withCommentCount(s.getCommentCount()))
        .toObservable();
  }

  public Observable<PostEntity> mergeShards(PostEntity entity, Key<Account> accountKey) {
    return Observable
        .fromIterable(entity.getShards())
        .cast(PostEntity.PostShard.class)
        .reduce((s1, s2) -> {
          s1.mergeWith(s2);
          s1.getBookmarkKeys().addAll(s2.getBookmarkKeys());
          return s1;
        })
        .map(s -> entity
            .withVoteCount(s.getVoteCount())
            .withCommentCount(s.getCommentCount())
            .withBookmarked(
                s.getBookmarkKeys().contains(Bookmark.createKey(accountKey, entity.getKey()))))
        .toObservable();
  }

  private Observable<PostEntity.PostShard> createShardsFromPostKey(Key<PostEntity> postKey) {
    return Observable
        .range(1, ShardConfig.POST_SHARD_COUNTER)
        .map(shardNum -> createShard(postKey, shardNum));
  }

  private PostEntity.PostShard createShard(Key<PostEntity> postKey, Integer shardNum) {
    return PostEntity.PostShard
        .builder()
        .id(ShardUtil.generateShardId(postKey, shardNum))
        .commentCount(0L)
        .voteCount(0L)
        .build();
  }
}
