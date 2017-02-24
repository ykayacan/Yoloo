package com.yoloo.backend.account;

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
public class AccountShardService implements Shardable<AccountShard, Account> {

  @Override
  public Map<Ref<AccountShard>, AccountShard> createShardMapWithRef(Iterable<Key<Account>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(this::createShardsFromPostKey)
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override
  public Map<Key<AccountShard>, AccountShard> createShardMapWithKey(Iterable<Key<Account>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(this::createShardsFromPostKey)
        .toMap(Key::create)
        .blockingGet();
  }

  @Override public Map<Ref<AccountShard>, AccountShard> createShardMapWithRef(Key<Account> key) {
    return createShardsFromPostKey(key)
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override public Map<Key<AccountShard>, AccountShard> createShardMapWithKey(Key<Account> key) {
    return createShardsFromPostKey(key)
        .toMap(Key::create)
        .blockingGet();
  }

  @Override
  public Key<AccountShard> getRandomShardKey(Key<Account> entityKey) {
    final int shardNum = new Random().nextInt(AccountShard.SHARD_COUNT - 1 + 1) + 1;
    return AccountShard.createKey(entityKey, shardNum);
  }

  @Override public Observable<List<Account>> mergeShards(Collection<Account> entities) {
    return Observable.fromIterable(entities)
        .flatMap(this::mergeShards)
        .toList(entities.size() == 0 ? 1 : entities.size())
        .toObservable();
  }

  @Override public Observable<Account> mergeShards(Account entity) {
    return Observable.fromIterable(entity.getShards())
        .cast(AccountShard.class)
        .reduce(this::reduceCounters)
        .map(s -> entity.withCounts(buildCounter(s)))
        .toObservable();
  }

  public AccountShard updateCounter(AccountShard shard, Update type) {
    switch (type) {
      case FOLLOWING_UP:
        shard.increaseFollowings();
        break;
      case FOLLOWING_DOWN:
        shard.decreaseFollowings();
        break;
      case FOLLOWER_UP:
        shard.increaseFollowers();
        break;
      case FOLLOWER_DOWN:
        shard.decreaseFollowers();
        break;
      case POST_UP:
        shard.increaseQuestions();
        break;
      case POST_DOWN:
        shard.decreaseQuestions();
        break;
    }
    return shard;
  }

  public Observable<AccountShard> merge(Account account) {
    return Observable.fromIterable(account.getShards())
        .reduce(this::reduceCounters)
        .toObservable();
  }

  private AccountShard reduceCounters(AccountShard s1, AccountShard s2) {
    return AccountShard.builder()
        .followerCount(s1.getFollowerCount() + s2.getFollowerCount())
        .followingCount(s1.getFollowingCount() + s2.getFollowingCount())
        .postCount(s1.getPostCount() + s2.getPostCount())
        .build();
  }

  private Account.Counts buildCounter(AccountShard shard) {
    return Account.Counts.builder()
        .followers(shard.getFollowerCount())
        .followings(shard.getFollowingCount())
        .questions(shard.getPostCount())
        .build();
  }

  private Observable<AccountShard> createShardsFromPostKey(Key<Account> accountKey) {
    return Observable
        .range(1, ShardConfig.ACCOUNT_SHARD_COUNTER)
        .map(shardNum -> createShard(accountKey, shardNum));
  }

  private AccountShard createShard(Key<Account> accountKey, Integer shardNum) {
    return AccountShard.builder()
        .id(ShardUtil.generateShardId(accountKey, shardNum))
        .followerCount(0L)
        .followingCount(0L)
        .postCount(0L)
        .build();
  }

  public enum Update {
    FOLLOWING_UP,
    FOLLOWING_DOWN,
    FOLLOWER_UP,
    FOLLOWER_DOWN,
    POST_UP,
    POST_DOWN
  }
}