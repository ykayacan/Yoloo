package com.yoloo.backend.feed;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountEntity;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.category.CategoryController;
import com.yoloo.backend.category.CategoryControllerFactory;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.game.GamificationService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.post.PostController;
import com.yoloo.backend.post.PostControllerFactory;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagController;
import com.yoloo.backend.tag.TagControllerFactory;
import com.yoloo.backend.util.TestBase;
import io.reactivex.Observable;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;

public class FeedControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Account owner;

  private Category budgetTravel;
  private Category europe;

  private Tag passport;

  private Tag visa;
  private Tag visa2;

  private PostController postController;
  private TagController tagController;
  private CategoryController categoryController;
  private FeedController feedController;

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

    postController = PostControllerFactory.of().create();
    tagController = TagControllerFactory.of().create();
    categoryController = CategoryControllerFactory.of().create();
    feedController = FeedControllerFactory.of().create();

    AccountEntity model = createAccount();

    owner = model.getAccount();
    DeviceRecord record = createRecord(owner);
    Tracker tracker = GamificationService.create().createTracker(owner.getKey());

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(owner)
        .add(record)
        .addAll(model.getShards().values())
        .add(tracker)
        .build();

    ofy().save().entities(saveList).now();

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    try {
      budgetTravel = categoryController.insertCategory("budget travel", Category.Type.THEME);
    } catch (ConflictException e) {
      e.printStackTrace();
    }
    try {
      europe = categoryController.insertCategory("europe", Category.Type.THEME);
    } catch (ConflictException e) {
      e.printStackTrace();
    }

    passport = tagController.insertGroup("passport");

    visa = tagController.insertTag("visa", "en", passport.getWebsafeId());
    visa2 = tagController.insertTag("visa2", "en", passport.getWebsafeId());
  }

  @Test
  public void testFeedList() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories = europe.getName() + "," + budgetTravel.getName();

    Post post = postController.insertQuestion("Test content", tags, categories, Optional.absent(),
        Optional.absent(), user);

    Feed feed = Feed.builder()
        .parent(post.getParent())
        .feedItemRef(Ref.create(post))
        .build();

    ofy().save().entity(feed).now();
  }

  private AccountEntity createAccount() {
    final Key<Account> ownerKey = fact().allocateId(Account.class);

    AccountShardService ass = AccountShardService.create();

    return Observable.range(1, AccountShard.SHARD_COUNT)
        .map(shardNum -> ass.createShard(ownerKey, shardNum))
        .toMap(Ref::create)
        .map(shardMap -> {
          Account account = Account.builder()
              .id(ownerKey.getId())
              .avatarUrl(new Link("Test avatar"))
              .email(new Email(USER_EMAIL))
              .username("Test user")
              .shardRefs(Lists.newArrayList(shardMap.keySet()))
              .created(DateTime.now())
              .build();

          return AccountEntity.builder()
              .account(account)
              .shards(shardMap)
              .build();
        })
        .blockingGet();
  }

  private DeviceRecord createRecord(Account owner) {
    return DeviceRecord.builder()
        .id(owner.getWebsafeId())
        .parentUserKey(owner.getKey())
        .regId(UUID.randomUUID().toString())
        .build();
  }
}
