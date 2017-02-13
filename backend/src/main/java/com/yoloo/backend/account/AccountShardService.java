package com.yoloo.backend.account;

import com.googlecode.objectify.Key;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;
import io.reactivex.Observable;
import java.util.List;
import java.util.Random;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class AccountShardService implements ShardService<Account, AccountShard> {

  @Override
  public List<Key<AccountShard>> createShardKeys(Iterable<Key<Account>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShardKeys)
        .toList()
        .blockingGet();
  }

  @Override
  public List<Key<AccountShard>> createShardKeys(final Key<Account> entityKey) {
    return Observable
        .range(1, AccountShard.SHARD_COUNT)
        .map(id -> AccountShard.createKey(entityKey, id))
        .toList()
        .blockingGet();
  }

  @Override
  public List<AccountShard> createShards(Iterable<Key<Account>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShards)
        .toList()
        .blockingGet();
  }

  @Override
  public List<AccountShard> createShards(final Key<Account> entityKey) {
    return Observable
        .range(1, AccountShard.SHARD_COUNT)
        .map(shardId -> createShard(entityKey, shardId))
        .toList()
        .blockingGet();
  }

  @Override
  public AccountShard createShard(Key<Account> entityKey, int shardNum) {
    return AccountShard.builder()
        .id(ShardUtil.generateShardId(entityKey, shardNum))
        .followers(0)
        .followings(0)
        .questions(0)
        .build();
  }

  @Override
  public Key<AccountShard> getRandomShardKey(Key<Account> entityKey) {
    final int shardNum = new Random().nextInt(AccountShard.SHARD_COUNT - 1 + 1) + 1;
    return AccountShard.createKey(entityKey, shardNum);
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

  private AccountShard reduceCounters(AccountShard s1,
      AccountShard s2) {
    return AccountShard.builder()
        .followers(s1.getFollowers() + s2.getFollowers())
        .followings(s1.getFollowings() + s2.getFollowings())
        .questions(s1.getQuestions() + s2.getQuestions())
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