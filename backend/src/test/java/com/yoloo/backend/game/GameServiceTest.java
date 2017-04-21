package com.yoloo.backend.game;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountBundle;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.util.TestBase;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameServiceTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Account owner;

  @Override public void setUpGAE() {
    super.setUpGAE();

    helper.setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);
  }

  @Override public void setUp() {
    super.setUp();

    AccountBundle model = createAccount();

    owner = model.getAccount();
    DeviceRecord record = createRecord(owner);
    Tracker tracker = GameService.create().createTracker(owner.getKey());

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(owner)
        .addAll(model.getShards().values())
        .add(tracker)
        .add(record)
        .build();

    ofy().save().entities(saveList).now();
  }

  @Test public void testGameInfoWithNoHistory_level0() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    GameService service = GameService.create();
    GameInfo info = service.getGameInfo(user);

    assertEquals("0", info.getLevel());
    assertEquals("1", info.getNextLevel());
    assertEquals(0, info.getPoints());
    assertEquals(300, info.getRequiredPoints());
    assertTrue(info.getHistories().isEmpty());
  }

  @Test public void testGameInfoWithNoHistory_level1() throws Exception {
    Tracker tracker = ofy().load().key(Tracker.createKey(owner.getKey())).now();

    tracker.setLevel(1);
    tracker.setPoints(320);

    ofy().save().entity(tracker).now();

    GameService service = GameService.create();
    GameInfo info =
        service.getGameInfo(new User(owner.getEmail().getEmail(), "", owner.getWebsafeId()));

    assertEquals("1", info.getLevel());
    assertEquals("2", info.getNextLevel());
    assertEquals(320, info.getPoints());
    assertEquals(180, info.getRequiredPoints());
    assertTrue(info.getHistories().isEmpty());
  }

  @Test public void testGameInfoWithHistory_level1() throws Exception {
    Tracker tracker = ofy().load().key(Tracker.createKey(owner.getKey())).now();
    tracker.setLevel(1);
    tracker.setPoints(320);

    Notification notification = Notification.builder()
        .receiverKey(owner.getKey())
        .action(Action.GAME)
        .payload("points", 320)
        .payload("bounties", 10)
        .created(DateTime.now())
        .build();

    ofy().save().entities(tracker, notification).now();

    GameService service = GameService.create();
    GameInfo info =
        service.getGameInfo(new User(owner.getEmail().getEmail(), "", owner.getWebsafeId()));

    assertEquals("1", info.getLevel());
    assertEquals("2", info.getNextLevel());
    assertEquals(320, info.getPoints());
    assertEquals(180, info.getRequiredPoints());
    assertEquals(1, info.getHistories().size());
    assertEquals(320, info.getHistories().get(0).getPoints());
    assertEquals(10, info.getHistories().get(0).getBounties());
  }

  private AccountBundle createAccount() {
    final Key<Account> ownerKey = fact().allocateId(Account.class);

    AccountShardService ass = AccountShardService.create();

    Map<Ref<AccountShard>, AccountShard> map = ass.createShardMapWithRef(ownerKey);

    Account account = Account.builder()
        .id(ownerKey.getId())
        .avatarUrl(new Link("Test avatar"))
        .email(new Email(USER_EMAIL))
        .username("Test user")
        .shardRefs(Lists.newArrayList(map.keySet()))
        .created(DateTime.now())
        .build();

    return AccountBundle.builder().account(account).shards(map).build();
  }

  private DeviceRecord createRecord(Account owner) {
    return DeviceRecord.builder()
        .id(owner.getWebsafeId())
        .parent(owner.getKey())
        .regId(UUID.randomUUID().toString())
        .build();
  }
}
