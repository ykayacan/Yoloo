package com.yoloo.backend.relationship;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountBundle;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.util.TestBase;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

public class RelationshipControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Account follower;
  private Account following;

  private RelationshipController relationshipController;

  @Override
  public void setUpGAE() {
    super.setUpGAE();

    helper.setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);
  }

  @Override
  public void setUp() {
    super.setUp();

    relationshipController = RelationshipControllerFactory.of().create();

    AccountBundle model1 = createAccount();
    AccountBundle model2 = createAccount();

    follower = model1.getAccount();
    following = model2.getAccount();

    DeviceRecord record1 = createRecord(follower);
    DeviceRecord record2 = createRecord(following);

    ImmutableList<Object> saveList = ImmutableList.builder()
        .add(follower)
        .add(following)
        .addAll(model1.getShards().values())
        .addAll(model2.getShards().values())
        .add(record1)
        .add(record2)
        .build();

    ofy().save().entities(saveList).now();
  }

  @Test
  public void testFollowAccount() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    AccountShardService shardService = AccountShardService.create();

    relationshipController.follow(following.getWebsafeId(), user);

    assertEquals(1, shardService.merge(follower).blockingSingle().getFollowingCount());
    assertEquals(1, shardService.merge(following).blockingSingle().getFollowerCount());

    Relationship relationship = ofy().load().type(Relationship.class).ancestor(follower.getKey()).first().now();

    assertEquals(follower.getKey(), relationship.getFollowerKey());
    assertEquals(following.getKey(), relationship.getFollowingKey());

    assertEquals(1, ofy().load().type(Relationship.class).ancestor(follower.getKey()).count());
  }

  @Test(expected = NotFoundException.class)
  public void testUnfollowAccount() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    AccountShardService shardService = AccountShardService.create();

    relationshipController.follow(following.getKey().toWebSafeString(), user);
    relationshipController.unfollow(following.getKey().toWebSafeString(), user);

    assertEquals(0, shardService.merge(follower).blockingSingle().getFollowingCount());
    assertEquals(0, shardService.merge(follower).blockingSingle().getFollowerCount());

    ofy().load().type(Relationship.class).ancestor(follower.getKey()).first().safe();
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

    return AccountBundle.builder()
        .account(account)
        .shards(map)
        .build();
  }

  private DeviceRecord createRecord(Account owner) {
    return DeviceRecord.builder()
        .id(owner.getWebsafeId())
        .parent(owner.getKey())
        .regId(UUID.randomUUID().toString())
        .build();
  }
}
