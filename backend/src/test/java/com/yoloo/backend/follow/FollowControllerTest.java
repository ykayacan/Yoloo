package com.yoloo.backend.follow;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountModel;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.util.TestBase;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class FollowControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Account follower;
  private Account following;

  private FollowController followController;

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

    followController = FollowContollerFactory.of().create();

    AccountModel model1 = createAccount();
    AccountModel model2 = createAccount();

    follower = model1.getAccount();
    following = model2.getAccount();

    DeviceRecord record1 = createRecord(follower);
    DeviceRecord record2 = createRecord(following);

    ImmutableList<Object> saveList = ImmutableList.builder()
        .add(follower)
        .add(following)
        .addAll(model1.getShards())
        .addAll(model2.getShards())
        .add(record1)
        .add(record2)
        .build();

    ofy().save().entities(saveList).now();
  }

  @Test
  public void testFollowAccount() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    AccountShardService shardService = AccountShardService.create();

    followController.follow(following.getWebsafeId(), user);

    assertEquals(1, shardService.merge(follower).blockingSingle().getFollowings());
    assertEquals(1, shardService.merge(following).blockingSingle().getFollowers());

    Follow follow = ofy().load().type(Follow.class).ancestor(follower.getKey()).first().now();

    assertEquals(follower.getKey(), follow.getParentUserKey());
    assertEquals(following.getKey(), follow.getFollowingKey());

    assertEquals(1, ofy().load().type(Follow.class).ancestor(follower.getKey()).count());
  }

  @Test(expected = NotFoundException.class)
  public void testUnfollowAccount() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    AccountShardService shardService = AccountShardService.create();

    followController.follow(following.getKey().toWebSafeString(), user);
    followController.unfollow(following.getKey().toWebSafeString(), user);

    assertEquals(0, shardService.merge(follower).blockingSingle().getFollowings());
    assertEquals(0, shardService.merge(follower).blockingSingle().getFollowers());

    ofy().load().type(Follow.class).ancestor(follower.getKey()).first().safe();
  }

  private AccountModel createAccount() {
    final Key<Account> ownerKey = fact().allocateId(Account.class);

    AccountShardService ass = AccountShardService.create();

    List<AccountCounterShard> shards = ass.createShards(ownerKey);

    List<Ref<AccountCounterShard>> refs = ShardUtil.createRefs(shards).toList().blockingGet();

    Account account = Account.builder()
        .id(ownerKey.getId())
        .avatarUrl(new Link("Test avatar"))
        .email(new Email(USER_EMAIL))
        .username("Test user")
        .shardRefs(refs)
        .created(DateTime.now())
        .build();

    return AccountModel.builder()
        .account(account)
        .shards(shards)
        .build();
  }

  private DeviceRecord createRecord(Account owner) {
    return DeviceRecord.builder()
        .id(owner.getWebsafeId())
        .parentUserKey(owner.getKey())
        .regId(UUID.randomUUID().toString())
        .build();
  }
}
