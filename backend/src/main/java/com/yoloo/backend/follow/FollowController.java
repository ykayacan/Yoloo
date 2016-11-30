package com.yoloo.backend.follow;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.notification.NotificationService;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public class FollowController extends Controller {

    private static final Logger logger =
            Logger.getLogger(FollowController.class.getName());

    /**
     * Maximum number of follow entity to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    @NonNull
    private FollowService followService;

    @NonNull
    private AccountShardService accountShardService;

    @NonNull
    private NotificationService notificationService;

    public void follow(String websafeFollowId, User user) {
        // Create user key from user id.
        final Key<Account> followerKey = Key.create(user.getUserId());

        // Create target user key from user id.
        final Key<Account> followingKey = Key.create(websafeFollowId);

        Follow follow = followService.create(followerKey, followingKey);

        Key<AccountCounterShard> followerShardKey =
                accountShardService.getRandomShardKey(followerKey);
        Key<AccountCounterShard> followingShardKey =
                accountShardService.getRandomShardKey(followingKey);

        Map<Key<Object>, Object> map = ofy().load()
                .keys(followerKey, followerShardKey, followingShardKey);

        //noinspection SuspiciousMethodCalls
        AccountCounterShard followerShard = (AccountCounterShard) map.get(followerShardKey);
        //noinspection SuspiciousMethodCalls
        AccountCounterShard followingShard = (AccountCounterShard) map.get(followingShardKey);

        followerShard = accountShardService
                .updateCounter(followerShard, AccountShardService.UpdateType.FOLLOWING_UP);
        followingShard = accountShardService
                .updateCounter(followingShard, AccountShardService.UpdateType.FOLLOWER_UP);

        // TODO: 29.11.2016 Send notification to following user.

        final ImmutableList<Object> saveList = ImmutableList.builder()
                .add(follow)
                .add(followerShard)
                .add(followingShard)
                .build();

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().save().entities(saveList).now();
            }
        });
    }

    public void unfollow(String websafeFollowId, User user) {
        // Create user key from user id.
        final Key<Account> followerKey = Key.create(user.getUserId());

        // Create target user key from user id.
        final Key<Account> followingKey = Key.create(websafeFollowId);

        final Key<Follow> followKey = ofy().load().type(Follow.class)
                .ancestor(followerKey).filter(Follow.FIELD_FOLLOWING_KEY + " =", followingKey)
                .keys().first().now();

        Key<AccountCounterShard> followerShardKey =
                accountShardService.getRandomShardKey(followerKey);
        Key<AccountCounterShard> followingShardKey =
                accountShardService.getRandomShardKey(followingKey);

        Map<Key<Object>, Object> map = ofy().load()
                .keys(followerKey, followerShardKey, followingShardKey);

        //noinspection SuspiciousMethodCalls
        AccountCounterShard followerShard = (AccountCounterShard) map.get(followerShardKey);
        //noinspection SuspiciousMethodCalls
        AccountCounterShard followingShard = (AccountCounterShard) map.get(followingShardKey);

        followerShard = accountShardService
                .updateCounter(followerShard, AccountShardService.UpdateType.FOLLOWING_DOWN);
        followingShard = accountShardService
                .updateCounter(followingShard, AccountShardService.UpdateType.FOLLOWER_DOWN);

        final ImmutableList<Object> saveList = ImmutableList.builder()
                .add(followerShard)
                .add(followingShard)
                .build();

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().defer().delete().key(followKey);
                ofy().defer().save().entities(saveList);
            }
        });
    }

    public CollectionResponse<Account> list(String websafeAccountId,
                                             FollowListType type,
                                             Optional<Integer> limit,
                                             Optional<String> cursor,
                                             User user) {
        // Create account key from websafe id.
        final Key<Account> followerKey = Key.create(websafeAccountId);

        // Init query fetch request.
        Query<Follow> query = ofy().load().type(Follow.class);

        if (type.equals(FollowListType.FOLLOWING)) {
            query = query.ancestor(followerKey);
        } else if (type.equals(FollowListType.FOLLOWER)) {
            query = query.filter(Follow.FIELD_FOLLOWING_KEY + " =", followerKey);
        }

        // Fetch items from beginning from cursor.
        query = cursor.isPresent()
                ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
                : query;

        // Limit items.
        query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

        final QueryResultIterator<Follow> qi = query.iterator();

        ImmutableList.Builder<Key<Account>> builder = ImmutableList.builder();
        while (qi.hasNext()) {
            if (type.equals(FollowListType.FOLLOWING)) {
                builder.add(qi.next().getFollowingKey());
            } else if (type.equals(FollowListType.FOLLOWER)) {
                builder.add(qi.next().getParentUserKey());
            }
        }

        final Collection<Account> accounts = ofy().load().keys(builder.build()).values();

        return CollectionResponse.<Account>builder()
                .setItems(accounts)
                .setNextPageToken(qi.getCursor().toWebSafeString())
                .build();
    }

    enum FollowListType {
        FOLLOWER, FOLLOWING
    }
}
