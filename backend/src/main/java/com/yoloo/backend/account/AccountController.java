package com.yoloo.backend.account;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.follow.Follow;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.size.ThumbSize;
import com.yoloo.backend.util.ServerConfig;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public final class AccountController extends Controller {

  private static final Logger logger =
      Logger.getLogger(AccountController.class.getName());

  /**
   * Maximum number of questions to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private AccountService accountService;

  private AccountShardService accountShardService;

  private GamificationService gamificationService;

  /**
   * Get account.
   *
   * @param user the user
   * @return the account
   */
  public Account get(User user) {
    return get(user.getUserId(), user);
  }

  /**
   * Get account.
   *
   * @param accountId the account id
   * @param user the user
   * @return the account
   */
  public Account get(String accountId, User user) {
    final Key<Account> targetAccountKey = Key.create(accountId);
    final Key<Account> currentAccountKey = Key.create(user.getUserId());

    final Key<Tracker> trackerKey = Tracker.createKey(targetAccountKey);

    // Fetch account.
    Map<Key<Object>, Object> fetched = ofy().load()
        .group(Account.ShardGroup.class)
        .keys(targetAccountKey, trackerKey);

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(targetAccountKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);

    final Key<Follow> followKey = ofy().load().type(Follow.class)
        .ancestor(currentAccountKey)
        .filter(Follow.FIELD_FOLLOWING_KEY + " =", targetAccountKey)
        .keys().first().now();

    return account
        .withFollowing(followKey != null)
        .withCounts(accountShardService.merge(account)
            .map(shard ->
                Account.Counts.builder()
                    .followers(shard.getFollowers())
                    .followings(shard.getFollowings())
                    .questions(shard.getQuestions())
                    .build())
            .blockingFirst())
        .withAchievements(
            Account.Achievements.builder()
                .level(tracker.getLevel())
                .bounties(tracker.getBounties())
                .points(tracker.getPoints()).build());
  }

  /**
   * Add account.
   *
   * @param locale the locale
   * @param request the request  @return the account
   */
  public Account add(String locale, Account.Gender gender, String topicIds,
      HttpServletRequest request) throws ConflictException {
    final String authHeader = request.getHeader(OAuth2.HeaderType.AUTHORIZATION);

    final String idToken = authHeader.split(" ")[1];

    Task<FirebaseToken> authTask = getFirebaseTask(idToken);
    awaitTask(authTask);

    FirebaseToken token = authTask.getResult();

    try {
      ofy().load().type(Account.class)
          .filter(Account.FIELD_EMAIL + " =", token.getEmail())
          .keys().first().safe();

      throw new ConflictException("User is already registered.");
    } catch (NotFoundException e) {
      AccountModel model =
          accountService.create(token, locale, gender, topicIds, accountShardService);

      Tracker tracker = gamificationService.create(model.getAccount().getKey());

      // Immutable helper list object to save all entities in a single db write.
      // For each single object use builder.addAdmin() method.
      // For each list object use builder.addAll() method.
      ImmutableSet<Object> saveList = ImmutableSet.builder()
          .add(model.getAccount())
          .addAll(model.getShards())
          .add(tracker)
          .build();

      return ofy().transact(() -> {
        Map<Key<Object>, Object> saved = ofy().save().entities(saveList).now();

        //noinspection SuspiciousMethodCalls
        Account account = (Account) saved.get(model.getAccount().getKey());

        return account
            .withCounts(Account.Counts.builder().build())
            .withAchievements(Account.Achievements.builder().build());
      });
    }
  }

  /**
   * Add admin account.
   *
   * @param request the request
   * @return the account
   * @throws ConflictException the conflict exception
   */
  public Account addAdmin(HttpServletRequest request) throws ConflictException {
    final String authHeader = request.getHeader(OAuth2.HeaderType.AUTHORIZATION);

    final String idToken = authHeader.split(" ")[1];

    Task<FirebaseToken> authTask = getFirebaseTask(idToken);
    awaitTask(authTask);

    FirebaseToken token = authTask.getResult();

    try {
      ofy().load().type(Account.class)
          .filter(Account.FIELD_EMAIL + " =", token.getEmail())
          .keys().first().safe();

      throw new ConflictException("User is already registered.");
    } catch (NotFoundException e) {
      AccountModel model = accountService.createAdmin(token);

      return ofy().transact(() -> {
        ofy().save().entities(model.getAccount()).now();

        return model.getAccount();
      });
    }
  }

  /**
   * Update account.
   *
   * @param accountId the account id
   * @param mediaId the media id
   * @param username the username
   * @param user the user
   * @return the account
   */
  public Account update(String accountId, Optional<String> mediaId,
      Optional<String> username, User user) {
    final Key<Account> accountKey = Key.create(accountId);

    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);

    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    keyBuilder.add(accountKey);
    keyBuilder.add(trackerKey);

    if (mediaId.isPresent()) {
      final Key<Media> mediaKey = Key.create(mediaId.get());
      keyBuilder.add(mediaKey);
    }

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    Map<Key<Object>, Object> fetched = ofy().load().group(Account.ShardGroup.class)
        .keys(batchKeys.toArray(new Key<?>[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);

    if (mediaId.isPresent()) {
      final Key<Media> mediaKey = Key.create(mediaId.get());

      //noinspection SuspiciousMethodCalls
      Media media = (Media) fetched.get(mediaKey);

      Media.Size size = new ThumbSize(media.getUrl());

      account = account.withAvatarUrl(new Link(size.getUrl()));
    }

    account = accountService.update(account, mediaId, username);

    ofy().save().entity(account);

    return account
        .withCounts(accountShardService.merge(account)
            .map(shard ->
                Account.Counts.builder()
                    .followers(shard.getFollowers())
                    .followings(shard.getFollowings())
                    .questions(shard.getQuestions())
                    .build())
            .blockingFirst())
        .withAchievements(
            Account.Achievements.builder()
                .level(tracker.getLevel())
                .bounties(tracker.getBounties())
                .points(tracker.getPoints()).build());
  }

  /**
   * Delete.
   *
   * @param user the user
   */
  public void delete(User user) {
    final Key<Account> accountKey = Key.create(user.getUserId());

    List<Key<AccountCounterShard>> shardKeys = accountShardService.createShardKeys(accountKey);

    // TODO: 16.12.2016 Remove shards keys.

    ofy().transact(() -> {
      List<Key<Object>> keys = ofy().load().ancestor(accountKey).keys().list();
      ImmutableSet<Key<?>> deleteList = ImmutableSet.<Key<?>>builder()
          .addAll(keys)
          .addAll(shardKeys)
          .build();

      if (ServerConfig.isDev()) {
        ofy().delete().keys(deleteList).now();
      } else {
        ofy().delete().keys(deleteList);
      }
    });
  }

  /**
   * Search accounts collection response.
   *
   * @param q the value
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<Account> searchAccounts(String q, Optional<String> cursor,
      Optional<Integer> limit, User user) {
    q = q.toLowerCase().trim();

    Query<Account> query = ofy().load().type(Account.class)
        .filter(Account.FIELD_USERNAME + " >=", q)
        .filter(Account.FIELD_USERNAME + " <", q + "\ufffd");

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    // Limit items.
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Account> qi = query.iterator();

    List<Account> accounts = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);
    while (qi.hasNext()) {
      // Add fetched objects to map. Because cursor iteration needs to be iterated.
      accounts.add(qi.next());
    }

    return CollectionResponse.<Account>builder()
        .setItems(accounts)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  public WrapperBoolean checkUsername(String username) {
    try {
      ofy().load().type(Account.class).filter(Account.FIELD_USERNAME + " =", username)
          .keys().first().safe();
      return new WrapperBoolean(true);
    } catch (NotFoundException e) {
      return new WrapperBoolean(false);
    }
  }

  private Task<FirebaseToken> getFirebaseTask(String idToken) {
    return FirebaseAuth.getInstance()
        .verifyIdToken(idToken)
        .addOnSuccessListener(decodedToken -> {
        });
  }

  private void awaitTask(Task<FirebaseToken> authTask) {
    try {
      Tasks.await(authTask);
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}