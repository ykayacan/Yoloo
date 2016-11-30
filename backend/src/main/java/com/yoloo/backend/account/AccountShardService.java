package com.yoloo.backend.account;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.account.AccountCounterShard.SHARD_COUNT;

@AllArgsConstructor(staticName = "newInstance")
public class AccountShardService {

    public ImmutableList<AccountCounterShard> createShards(final Key<Account> accountKey) {
        return Observable.range(1, SHARD_COUNT)
                .map(new Function<Integer, AccountCounterShard>() {
                    @Override
                    public AccountCounterShard apply(Integer shardId) throws Exception {
                        return createShard(accountKey, shardId);
                    }
                })
                .to(new Function<Observable<AccountCounterShard>, ImmutableList<AccountCounterShard>>() {
                    @Override
                    public ImmutableList<AccountCounterShard> apply(Observable<AccountCounterShard> o)
                            throws Exception {
                        return ImmutableList.copyOf(o.toList().blockingGet());
                    }
                });
    }

    public AccountCounterShard createShard(Key<Account> accountKey, int shardId) {
        return AccountCounterShard.builder()
                .id(AccountUtil.createShardId(accountKey, shardId))
                .followers(0)
                .followings(0)
                .questions(0)
                .build();
    }

    public ImmutableList<Key<AccountCounterShard>> getShardKeys(Collection<Key<Account>> keys) {
        return Observable.fromIterable(keys)
                .map(new Function<Key<Account>, List<Key<AccountCounterShard>>>() {
                    @Override
                    public List<Key<AccountCounterShard>> apply(final Key<Account> key)
                            throws Exception {
                        return Observable.range(1, SHARD_COUNT)
                                .map(new Function<Integer, Key<AccountCounterShard>>() {
                                    @Override
                                    public Key<AccountCounterShard> apply(Integer shardId)
                                            throws Exception {
                                        return Key.create(
                                                AccountCounterShard.class,
                                                AccountUtil.createShardId(key, shardId));
                                    }
                                }).toList().blockingGet();
                    }
                })
                .toList()
                .to(new Function<Single<List<List<Key<AccountCounterShard>>>>,
                        ImmutableList<Key<AccountCounterShard>>>() {
                    @Override
                    public ImmutableList<Key<AccountCounterShard>> apply(
                            Single<List<List<Key<AccountCounterShard>>>> listSingle)
                            throws Exception {
                        ImmutableList.Builder<Key<AccountCounterShard>> builder =
                                ImmutableList.builder();

                        for (List<Key<AccountCounterShard>> keys : listSingle.blockingGet()) {
                            builder = builder.addAll(keys);
                        }

                        return builder.build();
                    }
                });
    }

    public ImmutableList<Key<AccountCounterShard>> getShardKeys(Key<Account> key) {
        return Observable.just(key)
                .map(new Function<Key<Account>, List<Key<AccountCounterShard>>>() {
                    @Override
                    public List<Key<AccountCounterShard>> apply(final Key<Account> accountKey)
                            throws Exception {
                        return Observable.range(1, SHARD_COUNT)
                                .map(new Function<Integer, Key<AccountCounterShard>>() {
                                    @Override
                                    public Key<AccountCounterShard> apply(Integer shardId)
                                            throws Exception {
                                        return Key.create(
                                                AccountCounterShard.class,
                                                AccountUtil.createShardId(accountKey, shardId));
                                    }
                                }).toList().blockingGet();
                    }
                })
                .to(new Function<Observable<List<Key<AccountCounterShard>>>,
                        ImmutableList<Key<AccountCounterShard>>>() {
                    @Override
                    public ImmutableList<Key<AccountCounterShard>> apply(
                            Observable<List<Key<AccountCounterShard>>> listObservable)
                            throws Exception {
                        return ImmutableList.copyOf(listObservable.blockingFirst());
                    }
                });
    }

    public Key<AccountCounterShard> getRandomShardKey(final Key<Account> accountKey) {
        final int shardNum = new Random().nextInt(AccountCounterShard.SHARD_COUNT - 1 + 1) + 1;
        return Key.create(AccountCounterShard.class, AccountUtil.createShardId(accountKey, shardNum));
    }

    public AccountCounterShard updateCounter(AccountCounterShard shard, UpdateType type) {
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

    public enum UpdateType {
        FOLLOWING_UP,
        FOLLOWING_DOWN,
        FOLLOWER_UP,
        FOLLOWER_DOWN,
        POST_UP,
        POST_DOWN
    }
}
