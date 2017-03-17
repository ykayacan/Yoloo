package com.yoloo.backend.post;

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
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(staticName = "create")
public class PostShardService implements Shardable<PostShard, Post> {

  @Override
  public Map<Ref<PostShard>, PostShard> createShardMapWithRef(Iterable<Key<Post>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(this::createShardsFromPostKey)
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override
  public Map<Key<PostShard>, PostShard> createShardMapWithKey(Iterable<Key<Post>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(this::createShardsFromPostKey)
        .toMap(Key::create)
        .blockingGet();
  }

  @Override public Map<Ref<PostShard>, PostShard> createShardMapWithRef(Key<Post> key) {
    return createShardsFromPostKey(key)
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override public Map<Key<PostShard>, PostShard> createShardMapWithKey(Key<Post> key) {
    return createShardsFromPostKey(key)
        .toMap(Key::create)
        .blockingGet();
  }

  @Override public Key<PostShard> getRandomShardKey(Key<Post> entityKey) {
    final int shardNum = new Random().nextInt(ShardConfig.POST_SHARD_COUNTER - 1 + 1) + 1;
    return PostShard.createKey(entityKey, shardNum);
  }

  @Override public Observable<List<Post>> mergeShards(Collection<? extends Post> entities) {
    return Observable.fromIterable(entities)
        .flatMap(this::mergeShards)
        .toList(entities.size() == 0 ? 1 : entities.size())
        .toObservable();
  }

  @Override public Observable<Post> mergeShards(Post entity) {
    return Observable.fromIterable(entity.getShards())
        .cast(PostShard.class)
        .reduce((s1, s2) -> s1.addValues(s2.getComments(), s2.getVotes(), s2.getReports()))
        .map(s -> entity.withVoteCount(s.getVotes())
            .withCommentCount(s.getComments())
            .withReportCount(s.getReports()))
        .toObservable();
  }

  private Observable<PostShard> createShardsFromPostKey(Key<Post> postKey) {
    return Observable.range(1, ShardConfig.POST_SHARD_COUNTER)
        .map(shardNum -> createShard(postKey, shardNum));
  }

  private PostShard createShard(Key<Post> postKey, Integer shardNum) {
    return PostShard.builder()
        .id(ShardUtil.generateShardId(postKey, shardNum))
        .comments(0L)
        .votes(0L)
        .reports(0)
        .build();
  }
}
