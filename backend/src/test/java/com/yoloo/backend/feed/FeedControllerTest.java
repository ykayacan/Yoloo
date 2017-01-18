package com.yoloo.backend.feed;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountModel;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionController;
import com.yoloo.backend.question.QuestionControllerFactory;
import com.yoloo.backend.question.QuestionWrapper;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagController;
import com.yoloo.backend.tag.TagControllerFactory;
import com.yoloo.backend.tag.TagGroup;
import com.yoloo.backend.topic.Topic;
import com.yoloo.backend.topic.TopicController;
import com.yoloo.backend.topic.TopicControllerFactory;
import com.yoloo.backend.util.TestBase;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;

public class FeedControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Account owner;

  private Topic budgetTravel;
  private Topic europe;

  private TagGroup passport;

  private Tag visa;
  private Tag visa2;

  private QuestionController questionController;
  private TagController tagController;
  private TopicController topicController;
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

    questionController = QuestionControllerFactory.of().create();
    tagController = TagControllerFactory.of().create();
    topicController = TopicControllerFactory.of().create();
    feedController = FeedControllerFactory.of().create();

    AccountModel model = createAccount();

    owner = model.getAccount();
    DeviceRecord record = createRecord(owner);
    Tracker tracker = GamificationService.create().create(owner.getKey());

    ImmutableList<Object> saveList = ImmutableList.builder()
        .add(owner)
        .add(record)
        .addAll(model.getShards())
        .add(tracker)
        .build();

    ofy().save().entities(saveList).now();

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    budgetTravel = topicController.add("budget travel", Topic.Type.THEME, user);
    europe = topicController.add("europe", Topic.Type.THEME, user);

    passport = tagController.addGroup("passport", user);

    visa = tagController.addTag("visa", "en", passport.getWebsafeId(), user);
    visa2 = tagController.addTag("visa2", "en", passport.getWebsafeId(), user);
  }

  @Test
  public void testFeedList() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories = europe.getName() + "," + budgetTravel.getName();

    QuestionWrapper wrapper = QuestionWrapper.builder()
        .content("Test content")
        .tags(tags)
        .topics(categories)
        .bounty(0)
        .build();

    Question question = questionController.add(wrapper, user);

    Feed feed = Feed.builder()
        .parentUserKey(question.getParentUserKey())
        .feedItemRef(Ref.create(question))
        .build();

    ofy().save().entity(feed).now();

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
