package com.yoloo.backend.follow;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.FollowNotification;
import com.yoloo.backend.endpointsvalidator.Guard;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class FollowController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(FollowController.class.getName());

  /**
   * Maximum number of follow entity to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private AccountShardService accountShardService;

  private NotificationService notificationService;

  public void follow(String followingId, User user) {
    // Create user key from user id.
    final Key<Account> followerKey = Key.create(user.getUserId());

    // Create target user key from user id.
    final Key<Account> followingKey = Key.create(followingId);

    // Create record key for following account.
    final Key<DeviceRecord> recordKey = DeviceRecord.createKey(followingKey);

    Follow follow = followAccount(followerKey, followingKey);

    Key<AccountShard> followerShardKey = accountShardService.getRandomShardKey(followerKey);
    Key<AccountShard> followingShardKey = accountShardService.getRandomShardKey(followingKey);

    Map<Key<Object>, Object> fetched = ofy().load()
        .keys(followerKey, recordKey, followerShardKey, followingShardKey);

    //noinspection SuspiciousMethodCalls
    Account follower = (Account) fetched.get(followerKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);
    //noinspection SuspiciousMethodCalls
    AccountShard followerShard = (AccountShard) fetched.get(followerShardKey);
    //noinspection SuspiciousMethodCalls
    AccountShard followingShard = (AccountShard) fetched.get(followingShardKey);

    followerShard = accountShardService
        .updateCounter(followerShard, AccountShardService.Update.FOLLOWING_UP);
    followingShard = accountShardService
        .updateCounter(followingShard, AccountShardService.Update.FOLLOWER_UP);

    FollowNotification notification = FollowNotification.create(follower, record);

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(follow)
        .add(followerShard)
        .add(followingShard)
        .addAll(notification.getNotifications())
        .build();

    ofy().transact(() -> ofy().save().entities(saveList).now());

    notificationService.send(notification);
  }

  public void unfollow(String followingId, User user) throws NotFoundException {
    // Create user key from user id.
    final Key<Account> followerKey = Key.create(user.getUserId());

    // Create target user key from user id.
    final Key<Account> followingKey = Key.create(followingId);

    final Key<Follow> followKey = ofy().load().type(Follow.class)
        .ancestor(followerKey)
        .filter(Follow.FIELD_FOLLOWING_KEY + " =", followingKey)
        .keys().first().now();

    Guard.checkNotFound(followKey, "Follow is not found.");

    Key<AccountShard> followerShardKey =
        accountShardService.getRandomShardKey(followerKey);
    Key<AccountShard> followingShardKey =
        accountShardService.getRandomShardKey(followingKey);

    Map<Key<Object>, Object> fetched = ofy().load()
        .keys(followerKey, followerShardKey, followingShardKey);

    //noinspection SuspiciousMethodCalls
    AccountShard followerShard = (AccountShard) fetched.get(followerShardKey);
    //noinspection SuspiciousMethodCalls
    AccountShard followingShard = (AccountShard) fetched.get(followingShardKey);

    followerShard = accountShardService
        .updateCounter(followerShard, AccountShardService.Update.FOLLOWING_DOWN);
    followingShard = accountShardService
        .updateCounter(followingShard, AccountShardService.Update.FOLLOWER_DOWN);

    final ImmutableList<Object> saveList = ImmutableList.builder()
        .add(followerShard)
        .add(followingShard)
        .build();

    ofy().transact(() -> {
      ofy().defer().delete().key(followKey);
      ofy().defer().save().entities(saveList);
    });
  }

  public CollectionResponse<Account> list(String accountId, ListType type, Optional<Integer> limit,
      Optional<String> cursor) {
    // Create account key from websafe id.
    final Key<Account> followerKey = Key.create(accountId);

    // Init query fetch request.
    Query<Follow> query = ofy().load().type(Follow.class);

    if (type == ListType.FOLLOWING) {
      query = query.ancestor(followerKey);
    } else if (type == ListType.FOLLOWER) {
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
      if (type == ListType.FOLLOWING) {
        builder.add(qi.next().getFollowingKey());
      } else if (type == ListType.FOLLOWER) {
        builder.add(qi.next().getFollowerKey());
      }
    }

    final Collection<Account> accounts = ofy().load().keys(builder.build()).values();

    return CollectionResponse.<Account>builder()
        .setItems(accounts)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  private Follow followAccount(Key<Account> followerKey, Key<Account> followingKey) {
    return Follow.builder()
        .followerKey(followerKey)
        .followingKey(followingKey)
        .build();
  }
}