package com.yoloo.backend.group;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.group.sorter.GroupSorter;
import com.yoloo.backend.travelertype.TravelerTypeController;
import com.yoloo.backend.travelertype.TravelerTypeControllerFactory;
import io.reactivex.Single;
import ix.Ix;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
@AllArgsConstructor(staticName = "create")
public class TravelerGroupController extends Controller {

  private static final int DEFAULT_LIST_LIMIT = 7;

  private ImagesService imagesService;

  /**
   * Gets group.
   *
   * @param id the id
   * @param user the user
   * @return the group
   */
  public TravelerGroupEntity getGroup(@Nonnull String id, @Nonnull User user) {
    Key<Account> accountKey = Key.create(user.getUserId());
    Key<TravelerGroupEntity> groupKey = Key.create(id);

    Map<Key<Object>, Object> fetched = ofy().load().keys(accountKey, groupKey);

    Account account = (Account) fetched.get(accountKey);
    TravelerGroupEntity group = (TravelerGroupEntity) fetched.get(groupKey);

    for (Key<TravelerGroupEntity> key : account.getSubscribedGroupKeys()) {
      if (key.equivalent(groupKey)) {
        group = group.withSubscribed(true);
      }
    }

    return group;
  }

  /**
   * Insert group traveler group.
   *
   * @param displayName the display name
   * @param imageName the image name
   * @return the traveler group
   */
  public TravelerGroupEntity insertGroup(String displayName, String imageName) {
    final Key<TravelerGroupEntity> groupKey = TravelerGroupEntity.createKey(displayName);

    final String withIconUrl;
    final String withoutIconUrl;

    if (imageName.equals("dev")) {
      withIconUrl = Strings.nullToEmpty(imageName);
      withoutIconUrl = Strings.nullToEmpty(imageName);
    } else {
      ServingUrlOptions withIconOptions = ServingUrlOptions.Builder.withGoogleStorageFileName(
          MediaConfig.SERVE_GROUP_BUCKET_WITH_ICON + "/" + imageName.toLowerCase() + ".webp");

      withIconUrl = imagesService.getServingUrl(withIconOptions);

      ServingUrlOptions withoutIconOptions = ServingUrlOptions.Builder.withGoogleStorageFileName(
          MediaConfig.SERVE_GROUP_BUCKET_WITHOUT_ICON + "/" + imageName.toLowerCase() + ".webp");

      withoutIconUrl = imagesService.getServingUrl(withoutIconOptions);
    }

    TravelerGroupEntity entity = TravelerGroupEntity
        .builder()
        .id(groupKey.getName())
        .name(displayName)
        .imageWithIconUrl(new Link(withIconUrl))
        .imageWithoutIconUrl(new Link(withoutIconUrl))
        .rank(0.0D)
        .subscriberCount(0L)
        .postCount(0L)
        .build();

    return ofy().transact(() -> {
      ofy().save().entity(entity).now();

      return entity;
    });
  }

  /**
   * Update group traveler group.
   *
   * @param groupId the group id
   * @param name the name
   * @return the traveler group
   */
  public TravelerGroupEntity updateGroup(String groupId, Optional<String> name) {
    return Single
        .just(ofy().load().key(Key.<TravelerGroupEntity>create(groupId)).now())
        .map(entity -> name.isPresent() ? entity.withName(name.get()) : entity)
        .doOnSuccess(entity -> ofy().save().entity(entity).now())
        .blockingGet();
  }

  /**
   * List groups collection response.
   *
   * @param sorter the sorter
   * @param limit the limit
   * @param cursor the cursor
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<TravelerGroupEntity> listGroups(Optional<GroupSorter> sorter,
      Optional<Integer> limit, Optional<String> cursor, User user) {
    Account account = ofy().load().key(Key.<Account>create(user.getUserId())).now();

    Query<TravelerGroupEntity> query = ofy().load().type(TravelerGroupEntity.class);

    query = GroupSorter.sort(query, sorter.or(GroupSorter.DEFAULT));

    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<TravelerGroupEntity> qi = query.iterator();

    List<TravelerGroupEntity> groups = new ArrayList<>(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      TravelerGroupEntity group = qi.next();
      for (Key<TravelerGroupEntity> key : account.getSubscribedGroupKeys()) {
        if (group.getKey().equivalent(key)) {
          group = group.withSubscribed(true);
          break;
        }
      }
      groups.add(group);
    }

    return CollectionResponse.<TravelerGroupEntity>builder()
        .setItems(groups)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  public CollectionResponse<Account> listGroupUsers(String groupId, Optional<Integer> limit,
      Optional<String> cursor) {
    final Key<TravelerGroupEndpoint> groupKey = Key.create(groupId);

    Query<Account> query =
        ofy().load().type(Account.class).filter(Account.FIELD_SUBSCRIBED_GROUP_KEYS, groupKey);
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

  /**
   * Subscribe.
   *
   * @param groupId the group id
   * @param user the user
   */
  public void subscribe(@Nonnull String groupId, @Nonnull User user) {
    final Key<Account> accountKey = Key.create(user.getUserId());
    final Key<TravelerGroupEntity> groupKey = Key.create(groupId);

    Map<Key<Object>, Object> fetched = ofy().load().keys(accountKey, groupKey);

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    TravelerGroupEntity group = (TravelerGroupEntity) fetched.get(groupKey);

    ofy().transact(() -> {
      Account updatedAccount = account.toBuilder().subscribedGroupKey(groupKey).build();
      TravelerGroupEntity updatedGroup = group.withSubscriberCount(group.getSubscriberCount() + 1);

      ofy().save().entities(updatedAccount, updatedGroup).now();
    });
  }

  public void unsubscribe(@Nonnull String groupId, @Nonnull User user) {
    final Key<Account> accountKey = Key.create(user.getUserId());
    final Key<TravelerGroupEntity> groupKey = Key.create(groupId);

    Map<Key<Object>, Object> fetched = ofy().load().keys(accountKey, groupKey);

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    TravelerGroupEntity group = (TravelerGroupEntity) fetched.get(groupKey);

    ofy().transact(() -> {
      List<Key<TravelerGroupEntity>> updatedKeys = Ix
          .from(account.getSubscribedGroupKeys())
          .remove(key -> key.equivalent(groupKey))
          .toList();

      Account updatedAccount = account.withSubscribedGroupKeys(updatedKeys);
      TravelerGroupEntity updatedGroup = group.withSubscriberCount(group.getSubscriberCount() - 1);

      ofy().save().entities(updatedAccount, updatedGroup).now();
    });
  }

  /**
   * List subscribed groups collection.
   *
   * @param userId the user id
   * @return the collection
   */
  public Collection<TravelerGroupEntity> listSubscribedGroups(@Nonnull String userId) {
    final Key<Account> accountKey = Key.create(userId);

    Account account = ofy().load().key(accountKey).now();

    return Ix
        .from(ofy().load().keys(account.getSubscribedGroupKeys()).values())
        .map(entity -> entity.withSubscribed(true))
        .toList();
  }

  /**
   * Search groups collection.
   *
   * @param q the q
   * @return the collection
   */
  public Collection<TravelerGroupEntity> searchGroups(@Nonnull String q) {

    // We need to convert capitalize query word
    q = q.length() == 0 ? q : q.substring(0, 1).toUpperCase() + q.substring(1);
    q = q.trim();

    Query<TravelerGroupEntity> query = ofy()
        .load()
        .type(TravelerGroupEntity.class)
        .filter(TravelerGroupEntity.FIELD_NAME + " >=", q)
        .filter(TravelerGroupEntity.FIELD_NAME + " <", q + "\ufffd");

    query = query.limit(5);

    return Ix.just(query.iterator())
        .doOnNext(it -> System.out.println("Next? " + it.hasNext()))
        .filter(Iterator::hasNext)
        .map(Iterator::next)
        .toList();
  }

  public void setup() {
    TravelerGroupEntity activities = insertGroup("Activities", "activities");
    TravelerGroupEntity adventure = insertGroup("Adventure", "adventure");
    TravelerGroupEntity camping = insertGroup("Camping", "camping");
    TravelerGroupEntity culture = insertGroup("Culture", "culture");
    TravelerGroupEntity events = insertGroup("Events", "events");
    TravelerGroupEntity food_drink = insertGroup("Food & Drink", "food&drink");
    TravelerGroupEntity nightlife = insertGroup("Nightlife", "nightlife");
    TravelerGroupEntity soloTravel = insertGroup("Solo Travel", "solo_travel");
    TravelerGroupEntity studyAbroad = insertGroup("Study Abroad", "study_abroad");
    TravelerGroupEntity tours = insertGroup("Tours", "tours");

    TravelerTypeController controller = TravelerTypeControllerFactory.of().create();
    controller.insertTravelerType("Thrill-Seeker", "thrill-seeker",
        adventure.getWebsafeId() + "," + activities.getWebsafeId());
    controller.insertTravelerType("Escapist", "escapist",
        tours.getWebsafeId() + "," + camping.getWebsafeId());
    controller.insertTravelerType("Partier", "partier",
        nightlife.getWebsafeId() + "," + events.getWebsafeId());
    controller.insertTravelerType("Planner", "planner",
        soloTravel.getWebsafeId() + "," + culture.getWebsafeId());
    controller.insertTravelerType("No-Expense Traveler", "no-expense",
        camping.getWebsafeId() + "," + studyAbroad.getWebsafeId());
    controller.insertTravelerType("Guidebook Memorizer", "guidebook-memorizer",
        food_drink.getWebsafeId() + "," + culture.getWebsafeId());
    controller.insertTravelerType("Know-It-All", "know-it-all",
        soloTravel.getWebsafeId() + "," + culture.getWebsafeId());
    controller.insertTravelerType("Repeater", "repeater",
        tours.getWebsafeId() + "," + culture.getWebsafeId());
    controller.insertTravelerType("Solo Traveler", "solo-traveler",
        soloTravel.getWebsafeId() + "," + events.getWebsafeId());
    controller.insertTravelerType("Travel Mate Seeker", "travel-mate-seeker",
        studyAbroad.getWebsafeId() + "," + nightlife.getWebsafeId());
  }

  public void setupDev() {
    TravelerGroupEntity activities = insertGroup("Activities", "dev");
    TravelerGroupEntity adventure = insertGroup("Adventure", "dev");
    TravelerGroupEntity camping = insertGroup("Camping", "dev");
    TravelerGroupEntity culture = insertGroup("Culture", "dev");
    TravelerGroupEntity events = insertGroup("Events", "dev");
    TravelerGroupEntity food_drink = insertGroup("Food & Drink", "dev");
    TravelerGroupEntity nightlife = insertGroup("Nightlife", "dev");
    TravelerGroupEntity soloTravel = insertGroup("Solo Travel", "dev");
    TravelerGroupEntity studyAbroad = insertGroup("Study Abroad", "dev");
    TravelerGroupEntity tours = insertGroup("Tours", "dev");

    TravelerTypeController controller = TravelerTypeControllerFactory.of().create();
    controller.insertTravelerType("Thrill-Seeker", "dev",
        adventure.getWebsafeId() + "," + activities.getWebsafeId());
    controller.insertTravelerType("Escapist", "dev",
        tours.getWebsafeId() + "," + camping.getWebsafeId());
    controller.insertTravelerType("Partier", "dev",
        nightlife.getWebsafeId() + "," + events.getWebsafeId());
    controller.insertTravelerType("Planner", "dev",
        soloTravel.getWebsafeId() + "," + culture.getWebsafeId());
    controller.insertTravelerType("No-Expense Traveler", "dev",
        camping.getWebsafeId() + "," + studyAbroad.getWebsafeId());
    controller.insertTravelerType("Guidebook Memorizer", "dev",
        food_drink.getWebsafeId() + "," + culture.getWebsafeId());
    controller.insertTravelerType("Know-It-All", "dev",
        soloTravel.getWebsafeId() + "," + culture.getWebsafeId());
    controller.insertTravelerType("Repeater", "dev",
        tours.getWebsafeId() + "," + culture.getWebsafeId());
    controller.insertTravelerType("Solo Traveler", "dev",
        soloTravel.getWebsafeId() + "," + events.getWebsafeId());
    controller.insertTravelerType("Travel Mate Seeker", "dev",
        studyAbroad.getWebsafeId() + "," + nightlife.getWebsafeId());
  }
}
