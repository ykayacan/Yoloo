package com.yoloo.backend.account;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Email;
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
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.Constants;
import com.yoloo.backend.account.task.CreateUserFeedServlet;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.endpointsvalidator.Guard;
import com.yoloo.backend.follow.Follow;
import com.yoloo.backend.game.GamificationService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.Size;
import com.yoloo.backend.media.size.ThumbSize;
import com.yoloo.backend.util.KeyUtil;
import com.yoloo.backend.util.ServerConfig;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.account.AccountUtil.generateUsername;
import static com.yoloo.backend.account.AccountUtil.isUserRegistered;

@Log
@AllArgsConstructor(staticName = "create")
public final class AccountController extends Controller {

  /**
   * Maximum number of postCount to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private AccountShardService accountShardService;

  private GamificationService gamificationService;

  /**
   * Get account.
   *
   * @param accountId the account id
   * @param user the user
   * @return the account
   */
  public Account getAccount(String accountId, User user) {
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

    return account
        .withFollowing(isFollowing(targetAccountKey, currentAccountKey))
        .withCounts(accountShardService.merge(account).map(this::buildCounter).blockingFirst())
        .withDetail(buildDetail(tracker));
  }

  /**
   * Add account.
   *
   * @param locale the locale
   * @param gender the gender
   * @param categoryIds the topic ids
   * @param request the request  @return the account
   * @return the account
   * @throws ConflictException the conflict exception
   */
  public Account insertAccount(
      String locale,
      Account.Gender gender,
      String categoryIds,
      HttpServletRequest request) throws ConflictException, BadRequestException {

    final String authHeader =
        Guard.checkBadRequest(request.getHeader(OAuth2.HeaderType.AUTHORIZATION),
            "Authorization Header can not be null!");

    FirebaseToken token = getFirebaseToken(authHeader.split(" ")[1]);

    if (isUserRegistered(token)) {
      throw new ConflictException("User is already registered.");
    } else {
      AccountEntity entity = createAccountEntity(token, locale, gender, categoryIds);

      Tracker tracker = gamificationService.createTracker(entity.getAccount().getKey());

      ImmutableSet<Object> saveList = ImmutableSet.builder()
          .add(entity.getAccount())
          .addAll(entity.getShards().values())
          .add(tracker)
          .build();

      return ofy().transact(() -> {
        Map<Key<Object>, Object> saved = ofy().save().entities(saveList).now();

        CreateUserFeedServlet.addToQueue(entity.getAccount().getWebsafeId(), categoryIds);

        //noinspection SuspiciousMethodCalls
        Account account = (Account) saved.get(entity.getAccount().getKey());

        return account
            .withCounts(Account.Counts.builder().build())
            .withDetail(Account.Detail.builder().build());
      });
    }
  }

  /**
   * Add admin account.
   *
   * @return the account
   * @throws ConflictException the conflict exception
   */
  public Account insertAdmin() throws ConflictException {
    Account admin = ofy().load().key(Key.create(Account.class, 1L)).now();

    Guard.checkConflictRequest(admin, "Admin is already registered.");

    return ofy().transact(() -> {
      AccountEntity model = createAdminAccountEntity();

      ofy().save().entity(model.getAccount()).now();

      return ofy().load().key(model.getAccount().getKey()).now();
    });
  }

  /**
   * Update account.
   *
   * @param accountId the account id
   * @param mediaId the media id
   * @param username the username
   * @return the account
   */
  public Account updateAccount(String accountId, Optional<String> mediaId,
      Optional<String> username) {

    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    final Key<Account> accountKey = Key.create(accountId);
    keyBuilder.add(accountKey);

    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);
    keyBuilder.add(trackerKey);

    if (mediaId.isPresent()) {
      keyBuilder.add(Key.create(mediaId.get()));
    }

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    Map<Key<Object>, Object> fetched = ofy().load().group(Account.ShardGroup.class)
        .keys(batchKeys.toArray(new Key<?>[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);
    //noinspection SuspiciousMethodCalls
    Media media = (Media) fetched.get(Key.create(mediaId.get()));

    account = updateAccount(account, Optional.fromNullable(media), username);

    ofy().save().entity(account);

    return account
        .withCounts(accountShardService.merge(account).map(this::buildCounter).blockingFirst())
        .withDetail(buildDetail(tracker));
  }

  /**
   * Delete.
   *
   * @param user the user
   */
  public void deleteAccount(User user) {
    final Key<Account> accountKey = Key.create(user.getUserId());

    // TODO: 16.12.2016 Remove shards keys.

    ofy().transact(() -> {
      List<Key<Object>> keys = ofy().load().ancestor(accountKey).keys().list();
      ImmutableSet<Key<?>> deleteList = ImmutableSet.<Key<?>>builder()
          .addAll(keys)
          .addAll(accountShardService.createShardMapWithKey(accountKey).keySet())
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
   * @return the collection response
   */
  public CollectionResponse<Account> searchAccounts(String q, Optional<String> cursor,
      Optional<Integer> limit) {

    q = q.toLowerCase().trim();

    Query<Account> query = setupSearchQuery(q, cursor, limit);

    final QueryResultIterator<Account> qi = query.iterator();

    List<Account> accounts = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);
    while (qi.hasNext()) {
      accounts.add(qi.next());
    }

    return CollectionResponse.<Account>builder()
        .setItems(accounts)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  public WrapperBoolean checkUsername(String username) {
    return new WrapperBoolean(ofy().load().type(Account.class)
        .filter(Account.FIELD_USERNAME + " =", username)
        .keys().first().now() != null);
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
      log.info("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private FirebaseToken getFirebaseToken(String idToken) {
    Task<FirebaseToken> authTask = getFirebaseTask(idToken);
    awaitTask(authTask);

    return authTask.getResult();
  }

  private Account.Detail buildDetail(Tracker tracker) {
    return Account.Detail.builder()
        .level(tracker.getLevel())
        .bounties(tracker.getBounties())
        .points(tracker.getPoints()).build();
  }

  private Account.Counts buildCounter(AccountShard shard) {
    return Account.Counts.builder()
        .followers(shard.getFollowerCount())
        .followings(shard.getFollowingCount())
        .questions(shard.getPostCount())
        .build();
  }

  private boolean isFollowing(Key<Account> targetAccountKey, Key<Account> currentAccountKey) {
    return ofy().load().type(Follow.class)
        .ancestor(currentAccountKey)
        .filter(Follow.FIELD_FOLLOWING_KEY + " =", targetAccountKey)
        .keys().first().now() != null;
  }

  private Query<Account> setupSearchQuery(String q, Optional<String> cursor,
      Optional<Integer> limit) {
    Query<Account> query = ofy().load().type(Account.class)
        .filter(Account.FIELD_USERNAME + " >=", q)
        .filter(Account.FIELD_USERNAME + " <", q + "\ufffd");

    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    return query.limit(limit.or(DEFAULT_LIST_LIMIT));
  }

  private AccountEntity createAccountEntity(FirebaseToken token, String locale,
      Account.Gender gender, String categoryIds) {

    final Key<Account> accountKey = factory().allocateId(Account.class);

    Map<Ref<AccountShard>, AccountShard> shardMap =
        accountShardService.createShardMapWithRef(accountKey);

    Account account = Account.builder()
        .id(accountKey.getId())
        .username(generateUsername(token))
        .realname(token.getName())
        .firebaseUUID(token.getUid())
        .email(new Email(token.getEmail()))
        .avatarUrl(new Link(token.getPicture()))
        .locale(locale)
        .gender(gender)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .categoryKeys(KeyUtil.extractKeysFromIds2(categoryIds, ","))
        .counts(Account.Counts.builder().build())
        .detail(Account.Detail.builder().build())
        .created(DateTime.now())
        .build();

    return AccountEntity.builder()
        .account(account)
        .shards(shardMap)
        .build();
  }

  private AccountEntity createAdminAccountEntity() {
    Account account = Account.builder()
        .id(1L)
        .username(Constants.ADMIN_USERNAME)
        .email(new Email(Constants.ADMIN_EMAIL))
        .created(DateTime.now())
        .build();

    return AccountEntity.builder()
        .account(account)
        .build();
  }

  private Account updateAccount(Account account, Optional<Media> media, Optional<String> username) {
    if (username.isPresent()) {
      account = account.withUsername(username.get());
    }

    if (media.isPresent()) {
      Size size = ThumbSize.of(media.get().getUrl());

      account = account.withAvatarUrl(new Link(size.getUrl()));
    }

    return account;
  }
}