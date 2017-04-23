package com.yoloo.backend.group;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.util.TestBase;
import ix.Ix;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TravelerGroupControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private static final String TEST_IMG = "dev_test";

  private User user;

  private TravelerGroupEndpoint endpoint;

  @Override
  public void setUpGAE() {
    super.setUpGAE();

    helper
        .setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);

    Account account = createAccount();

    user = new User(USER_EMAIL, USER_AUTH_DOMAIN, account.getWebsafeId());

    endpoint = new TravelerGroupEndpoint();
  }

  @Test
  public void testAddGroup() throws Exception {
    TravelerGroupEntity group = endpoint.insert("Adventure", TEST_IMG, user);

    assertEquals("Adventure", group.getName());
    assertEquals(0L, group.getPostCount());
    assertEquals(0.0D, group.getRank(), 0);
    assertEquals(ShardConfig.GROUP_SHARD_COUNTER, group.getShards().size());
  }

  @Test(expected = ConflictException.class)
  public void testAddGroupConflict() throws Exception {
    endpoint.insert("Adventure", TEST_IMG, user);
    endpoint.insert("Adventure", TEST_IMG, user);
  }

  @Test
  public void testUpdateGroup() throws Exception {
    TravelerGroupEntity group = endpoint.insert("Adventure", TEST_IMG, user);
    TravelerGroupEntity updated = endpoint.update(group.getWebsafeId(), "New Life", user);

    assertEquals("New Life", updated.getName());
  }

  @Test
  public void subscribe() throws Exception {
    TravelerGroupEntity group = endpoint.insert("Adventure", TEST_IMG, user);

    endpoint.subscribe(group.getWebsafeId(), user);

    TravelerGroupEntity adventure = ofy().load().entity(group).now();

    assertTrue(adventure.getPostCount() == 1L);
  }

  @Test
  public void testListGroups() throws Exception {
    endpoint.insert("Adventure", TEST_IMG, user);
    endpoint.insert("Activities", TEST_IMG, user);
    endpoint.insert("Tours", TEST_IMG, user);

    CollectionResponse<TravelerGroupEntity> response = endpoint.list(null, null, null, null);

    assertEquals(3, response.getItems().size());

    Ix.from(response.getItems()).foreach(group -> {
      assertEquals(0L, group.getPostCount());
      assertEquals(0.0D, group.getRank(), 0);
      assertEquals(ShardConfig.GROUP_SHARD_COUNTER, group.getShards().size());
    });
  }

  private Account createAccount() {
    final Key<Account> ownerKey = fact().allocateId(Account.class);

    AccountShardService ass = AccountShardService.create();

    Map<Ref<AccountShard>, AccountShard> map = ass.createShardMapWithRef(ownerKey);

    return Account
        .builder()
        .id(ownerKey.getId())
        .avatarUrl(new Link("Test avatar"))
        .email(new Email(USER_EMAIL))
        .username("Test user")
        .shardRefs(Lists.newArrayList(map.keySet()))
        .created(DateTime.now())
        .shardMap(map)
        .build();
  }
}
