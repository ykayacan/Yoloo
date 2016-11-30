package com.yoloo.backend.account;

import com.google.api.server.spi.auth.common.User;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseToken;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Work;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.gamification.GamificationService;

import java.util.Map;
import java.util.logging.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public final class AccountController extends Controller {

    private static final Logger logger =
            Logger.getLogger(AccountController.class.getName());

    @NonNull
    private AccountService accountService;

    @NonNull
    private AccountShardService accountShardService;

    @NonNull
    private GamificationService gamificationService;

    private Account get(String websafeAccountId, User user) {
        final Key<Account> accountKey = Key.create(websafeAccountId);

        // Fetch account.
        Account account = ofy().load().key(accountKey).now();

        account = AccountUtil.aggregateCounts(account, accountShardService);

        return account;
    }

    public Account add(FirebaseToken token) {
        try {
            Key<Account> accountKey = ofy().load().type(Account.class)
                    .filter(Account.FIELD_EMAIL + " =", token.getEmail())
                    .keys().first().safe();

            return get(accountKey.toWebSafeString(), new User("", ""));
        } catch (NotFoundException e) {
            // Allocate a new Account key.
            final Key<Account> newUserKey = ofy().factory().allocateId(Account.class);

            // Create account.
            final Account account = accountService.create(newUserKey, token);

            final AccountDetail detail = accountService.createDetail(newUserKey);

            // TODO: 28.11.2016 Implement account detail.

            // Create shards.
            ImmutableList<AccountCounterShard> shards =
                    accountShardService.createShards(newUserKey);

            // Immutable helper list object to save all entities in a single db write.
            // For each single object use builder.add() method.
            // For each list object use builder.addAll() method.
            final ImmutableList<Object> saveList = ImmutableList.builder()
                    .add(account)
                    .addAll(shards)
                    .add(detail)
                    .build();

            return ofy().transact(new Work<Account>() {
                @Override
                public Account run() {
                    Map<Key<Object>, Object> map = ofy().save().entities(saveList).now();
                    //noinspection SuspiciousMethodCalls
                    return (Account) map.get(account);
                }
            });
        }
    }
}
