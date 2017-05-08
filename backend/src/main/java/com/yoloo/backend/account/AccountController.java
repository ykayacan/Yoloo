package com.yoloo.backend.account;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.Constants;
import com.yoloo.backend.account.task.CreateUserFeedServlet;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentShard;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.country.Country;
import com.yoloo.backend.country.CountryService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.endpointsvalidator.Guard;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.media.Size;
import com.yoloo.backend.media.size.ThumbSize;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.relationship.Relationship;
import com.yoloo.backend.travelertype.TravelerTypeEntity;
import com.yoloo.backend.util.StringUtil;
import ix.Ix;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.account.AccountUtil.isUserRegistered;

@Log
@AllArgsConstructor(staticName = "create")
public final class AccountController extends Controller {

  private static final int DEFAULT_LIST_LIMIT = 20;

  private AccountShardService accountShardService;

  private GameService gameService;

  private MediaService mediaService;

  private CountryService countryService;

  public Account getAccount(String accountId, User user) {
    final Key<Account> targetAccountKey = Key.create(accountId);
    final Key<Account> currentAccountKey = Key.create(user.getUserId());

    final Key<Tracker> trackerKey = Tracker.createKey(targetAccountKey);

    // Fetch account.
    Map<Key<Object>, Object> fetched =
        ofy().load().group(Account.ShardGroup.class).keys(targetAccountKey, trackerKey);

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(targetAccountKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);

    return account
        .withLevelTitle(tracker.getTitle())
        .withFollowing(isFollowing(targetAccountKey, currentAccountKey))
        .withCounts(accountShardService.merge(account).map(this::buildCounter).blockingFirst())
        .withDetail(buildDetail(tracker));
  }

  public Account insertAccount(HttpServletRequest request)
      throws ConflictException, BadRequestException {

    final String authHeader = request.getHeader(OAuth2.HeaderType.AUTHORIZATION);
    final String base64Payload = StringUtil.split(authHeader, " ").get(1);

    final AccountJsonPayload payload = AccountJsonPayload.from(base64Payload);

    if (isUserRegistered(payload.getEmail())) {
      throw new ConflictException("User is already registered.");
    } else {
      Account account = createAccountFromPayload(payload);

      Tracker tracker = gameService.createTracker(account.getKey());

      return ofy().transact(() -> {
        ImmutableList<Object> saveList = ImmutableList
            .builder()
            .add(account)
            .addAll(account.getShardMap().values())
            .add(tracker)
            .build();

        ofy().save().entities(saveList).now();

        final String subscribedGroupIds = Stream
            .of(account.getPreInterestedGroupKeys())
            .map(Key::toWebSafeString)
            .collect(Collectors.joining(","));

        CreateUserFeedServlet.addToQueue(account.getWebsafeId(), subscribedGroupIds);

        return account
            .withCounts(Account.Counts.builder().build())
            .withDetail(Account.Detail.builder().build());
      });
    }
  }

  public Account insertAccountTest(String token) throws ConflictException, BadRequestException {

    AccountJsonPayload payload = AccountJsonPayload.from(token);

    if (isUserRegistered(payload.getEmail())) {
      throw new ConflictException("User is already registered.");
    } else {
      Account account = createAccountFromPayload(payload);

      Collection<TravelerGroupEntity> groups =
          ofy().load().keys(account.getSubscribedGroupKeys()).values();

      List<TravelerGroupEntity> updatedGroups = Ix
          .from(groups)
          .map(entity -> entity.withSubscriberCount(entity.getSubscriberCount() + 1))
          .toList();

      Tracker tracker = gameService.createTracker(account.getKey());

      ImmutableList<Object> saveList = ImmutableList
          .builder()
          .add(account)
          .addAll(account.getShardMap().values())
          .addAll(updatedGroups)
          .add(tracker)
          .build();

      return ofy().transact(() -> {
        ofy().save().entities(saveList).now();

        final String stringifiedSubscribedIds = Stream
            .of(account.getSubscribedGroupKeys())
            .map(Key::toWebSafeString)
            .collect(Collectors.joining(","));

        CreateUserFeedServlet.addToQueue(account.getWebsafeId(), stringifiedSubscribedIds);

        return account
            .withCounts(Account.Counts.builder().build())
            .withDetail(Account.Detail.builder().build());
      });
    }
  }

  private Account createAccountFromPayload(AccountJsonPayload payload) {
    final Key<Account> accountKey = factory().allocateId(Account.class);

    Map<Ref<AccountShard>, AccountShard> shardMap =
        accountShardService.createShardMapWithRef(accountKey);

    Country country = countryService.getCountry(payload.getCountryCode());

    List<Key<TravelerGroupEntity>> groupKeys = findGroupKeys(payload.getTravelerTypeIds());

    return Account
        .builder()
        .id(accountKey.getId())
        .username(payload.getUsername())
        .realname(payload.getRealname())
        .email(new Email(payload.getEmail()))
        .avatarUrl(new Link(payload.getProfileImageUrl()))
        .langCode(payload.getLangCode())
        .country(country)
        .preInterestedGroupKeys(groupKeys)
        .birthDate(new DateTime(payload.getBirthday()))
        .gender(Account.Gender.UNSPECIFIED)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .counts(Account.Counts.builder().build())
        .detail(Account.Detail.builder().build())
        .created(DateTime.now())
        .shardMap(shardMap)
        .build();
  }

  public Account insertAdmin() throws ConflictException {
    final long id = ofy().factory().allocateId(Account.class).getId();
    Account admin = ofy().load().key(Key.create(Account.class, id)).now();

    Guard.checkConflictRequest(admin, "Admin is already registered.");

    return ofy().transact(() -> {
      AccountBundle model = createAdminAccountEntity();

      ofy().save().entity(model.getAccount()).now();

      return ofy().load().key(model.getAccount().getKey()).now();
    });
  }

  public Account insertTestAccount() {
    return ofy().transact(() -> {
      AccountBundle entity = createTestAccountEntity();
      Tracker tracker = gameService.createTracker(entity.getAccount().getKey());
      DeviceRecord record = DeviceRecord
          .builder()
          .id(entity.getAccount().getWebsafeId())
          .parent(entity.getAccount().getKey())
          .regId("")
          .build();

      ofy().save().entities(entity.getAccount(), tracker, record).now();
      ofy().save().entities(entity.getShards().values()).now();

      return ofy().load().key(entity.getAccount().getKey()).now();
    });
  }

  public Account updateAccount(String accountId, Optional<String> mediaId,
      Optional<String> username, Optional<String> realName, Optional<String> email,
      Optional<String> url, Optional<String> bio, Optional<Account.Gender> gender,
      Optional<String> visitedCountryCode, Optional<String> countryCode) {

    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    final Key<Account> accountKey = Key.create(accountId);
    keyBuilder.add(accountKey);

    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);
    keyBuilder.add(trackerKey);

    if (mediaId.isPresent()) {
      keyBuilder.add(Key.create(mediaId.get()));
    }

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    Map<Key<Object>, Object> fetched = ofy()
        .load()
        .group(Account.ShardGroup.class)
        .keys(batchKeys.toArray(new Key<?>[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);

    return Ix
        .just(account)
        .map(updated -> {
          if (username.isPresent()) {
            updated = updated.withUsername(username.get());
          }

          if (realName.isPresent()) {
            updated = updated.withRealname(realName.get());
          }

          if (email.isPresent()) {
            updated = updated.withEmail(new Email(email.get()));
          }

          if (mediaId.isPresent()) {
            MediaEntity mediaEntity = (MediaEntity) fetched.get(Key.create(mediaId.get()));
            Size size = ThumbSize.of(mediaEntity.getUrl());
            updated = updated.withAvatarUrl(new Link(size.getUrl()));
          }

          if (countryCode.isPresent()) {
            Country country = countryService.getCountry(countryCode.get());
            updated = updated.withCountry(country);
          }

          if (visitedCountryCode.isPresent()) {
            Country country = countryService.getCountry(visitedCountryCode.get());

            Set<Country> visitedCountries = updated.getVisitedCountries();
            if (visitedCountries == null) {
              visitedCountries = new HashSet<>();
            }
            visitedCountries.add(country);

            updated = updated.withVisitedCountries(visitedCountries);
          }

          if (url.isPresent()) {
            updated = updated.withWebsiteUrl(new Link(url.get()));
          }

          if (bio.isPresent()) {
            updated = updated.withBio(bio.get());
          }

          if (gender.isPresent()) {
            updated = updated.withGender(gender.get());
          }

          return updated;
        })
        .doOnNext(updated -> ofy().transact(() -> ofy().save().entity(updated)))
        .map(updated -> updated
            .withCounts(accountShardService.merge(updated).map(this::buildCounter).blockingFirst())
            .withDetail(buildDetail(tracker)))
        .single();
  }

  public void deleteAccount(String accountId) {
    final Key<Account> accountKey = Key.create(accountId);

    List<Key<Object>> keys = ofy().load().ancestor(accountKey).keys().list();

    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);

    List<Key<MediaEntity>> mediaKeys =
        ofy().load().type(MediaEntity.class).ancestor(accountKey).keys().list();

    List<Key<Comment>> commentKeys =
        ofy().load().type(Comment.class).ancestor(accountKey).keys().list();
    Set<Key<CommentShard>> commentShardKeys =
        CommentShardService.create().createShardMapWithKey(commentKeys).keySet();

    List<Object> saveList = new ArrayList<>();
    Query<PostEntity> postQuery = ofy().load().type(PostEntity.class);
    for (Key<Comment> commentKey : commentKeys) {
      postQuery = postQuery.filter(PostEntity.FIELD_ACCEPTED_COMMENT_KEY, commentKey);
    }
    List<PostEntity> posts = postQuery.list();

    if (!posts.isEmpty()) {
      posts = Ix.from(posts).map(post -> post.withAcceptedCommentKey(null)).toList();
      saveList.addAll(posts);
    }

    Account account = ofy().load().key(accountKey).now();
    if (account.getSubscribedGroupKeys() != null) {
      Collection<TravelerGroupEntity> groups =
          ofy().load().keys(account.getSubscribedGroupKeys()).values();

      List<TravelerGroupEntity> updatedGroups = Ix
          .from(groups)
          .map(entity -> entity.withSubscriberCount(entity.getSubscriberCount() - 1))
          .toList();

      saveList.addAll(updatedGroups);
    }

    ImmutableSet<Key<?>> deleteList = ImmutableSet.<Key<?>>builder()
        .addAll(keys)
        .add(trackerKey)
        .addAll(accountShardService.createShardMapWithKey(accountKey).keySet())
        .addAll(commentShardKeys)
        .build();

    ofy().defer().delete().keys(deleteList);
    ofy().defer().save().entities(saveList);

    mediaService.deleteMedias(mediaKeys);
  }

  public CollectionResponse<Account> searchAccounts(String q, Optional<String> cursor,
      Optional<Integer> limit) {

    q = q.toLowerCase().trim();

    Query<Account> query = ofy()
        .load()
        .type(Account.class)
        .filter(Account.FIELD_USERNAME + " >=", q)
        .filter(Account.FIELD_USERNAME + " <", q + "\ufffd");

    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Account> qi = query.iterator();

    List<Account> accounts = new ArrayList<>(DEFAULT_LIST_LIMIT);
    while (qi.hasNext()) {
      accounts.add(qi.next());
    }

    return CollectionResponse.<Account>builder()
        .setItems(accounts)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  public CollectionResponse<Account> listRecommendedUsers(Optional<String> cursor,
      Optional<Integer> limit, User user) {
    final Key<Account> accountKey = Key.create(user.getUserId());

    Query<Account> query = ofy().load().type(Account.class).order("-" + Account.FIELD_CREATED);

    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Account> qi = query.iterator();

    List<Account> accounts = new ArrayList<>(DEFAULT_LIST_LIMIT);
    while (qi.hasNext()) {
      Account account = qi.next();
      if (!account.getKey().equivalent(accountKey)) {
        accounts.add(account);
      }
    }

    if (accounts.isEmpty()) {
      return CollectionResponse.<Account>builder().build();
    }

    return CollectionResponse.<Account>builder()
        .setItems(accounts)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  public CollectionResponse<Account> listNewUsers(Optional<String> cursor, Optional<Integer> limit,
      User user) {

    Key<Account> accountKey = Key.create(user.getUserId());

    List<Key<Account>> followingKeys = Ix
        .from(ofy().load().type(Relationship.class).ancestor(accountKey).list())
        .map(Relationship::getFollowingKey)
        .toList();

    Query<Account> query = ofy().load().type(Account.class).order("-" + Account.FIELD_CREATED);

    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Account> qi = query.iterator();

    return Ix
        .just(qi)
        .filter(Iterator::hasNext)
        .map(Iterator::next)
        .filter(
            account -> !account.getWebsafeId().equals(user.getUserId()) && !followingKeys.contains(
                account.getKey()))
        .collectToList()
        .map(accounts -> CollectionResponse.<Account>builder()
            .setItems(accounts)
            .setNextPageToken(qi.getCursor().toWebSafeString())
            .build())
        .single();
  }

  public WrappedBoolean checkUsername(String username) {
    return new WrappedBoolean(ofy()
        .load()
        .type(Account.class)
        .filter(Account.FIELD_USERNAME + " =", username)
        .keys()
        .first()
        .now() == null);
  }

  public WrappedBoolean checkEmail(String email) {
    return new WrappedBoolean(ofy()
        .load()
        .type(Account.class)
        .filter(Account.FIELD_EMAIL + " =", email)
        .keys()
        .first()
        .now() == null);
  }

  private Account.Detail buildDetail(Tracker tracker) {
    return Account.Detail
        .builder()
        .level(tracker.getLevel())
        .bounties(tracker.getBounties())
        .points(tracker.getPoints())
        .build();
  }

  private Account.Counts buildCounter(AccountShard shard) {
    return Account.Counts
        .builder()
        .followers(shard.getFollowerCount())
        .followings(shard.getFollowingCount())
        .questions(shard.getPostCount())
        .build();
  }

  private boolean isFollowing(Key<Account> targetAccountKey, Key<Account> currentAccountKey) {
    return ofy()
        .load()
        .type(Relationship.class)
        .ancestor(currentAccountKey)
        .filter(Relationship.FIELD_FOLLOWING_KEY + " =", targetAccountKey)
        .keys()
        .first()
        .now() != null;
  }

  private AccountBundle createAdminAccountEntity() {
    Account account = Account
        .builder()
        .id(1L)
        .username(Constants.ADMIN_USERNAME)
        .email(new Email(Constants.ADMIN_EMAIL))
        .created(DateTime.now())
        .build();

    return AccountBundle.builder().account(account).build();
  }

  private AccountBundle createTestAccountEntity() {
    final Key<Account> testKey = Key.create(Account.class, 1L);

    Map<Ref<AccountShard>, AccountShard> shardMap =
        accountShardService.createShardMapWithRef(testKey);

    Account account = Account
        .builder()
        .id(testKey.getId())
        .username("test")
        .realname("test")
        .email(new Email("test@test.com"))
        .avatarUrl(new Link(""))
        .langCode("")
        .gender(Account.Gender.UNSPECIFIED)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .counts(Account.Counts.builder().build())
        .detail(Account.Detail.builder().build())
        .created(DateTime.now())
        .build();

    return AccountBundle.builder().account(account).shards(shardMap).build();
  }

  public List<Key<TravelerGroupEntity>> findGroupKeys(@Nonnull List<String> travelerTypeIds) {
    return Ix
        .from(travelerTypeIds)
        .map(Key::<TravelerTypeEntity>create)
        .collectToList()
        .flatMap(keys -> ofy().load().keys(keys).values())
        .map(TravelerTypeEntity::getGroupKeys)
        .flatMap(Ix::from)
        .distinct()
        .toList();
  }
}