package com.yoloo.backend.account;

import com.googlecode.objectify.Key;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountUtil {

    public static Account aggregateCounts(Account account, AccountShardService service) {
        List<Key<AccountCounterShard>> shardKeys = service.getShardKeys(account.getKey());
        final Map<Key<AccountCounterShard>, AccountCounterShard> shardMap =
                ofy().load().keys(shardKeys);

        long followings = 0L;
        long followers = 0L;
        long questions = 0L;

        for (int i = 1; i <= AccountCounterShard.SHARD_COUNT; i++) {
            Key<AccountCounterShard> shardKey =
                    Key.create(AccountCounterShard.class, createShardId(account.getKey(), i));

            if (shardMap.containsKey(shardKey)) {
                AccountCounterShard shard = shardMap.get(shardKey);

                followings = shard.calculateFollowings(followings);
                followers = shard.calculateFollowers(followers);
                questions = shard.calculateQuestions(questions);

                account = account
                        .withFollowings(followings < 0 ? 0 : followings)
                        .withFollowers(followers < 0 ? 0 : followers)
                        .withQuestions(questions < 0 ? 0 : questions);
            }
        }

        return account;
    }

    public static String createShardId(Key<Account> accountKey, int shardNum) {
        return accountKey.toWebSafeString() + ":" + String.valueOf(shardNum);
    }
}
