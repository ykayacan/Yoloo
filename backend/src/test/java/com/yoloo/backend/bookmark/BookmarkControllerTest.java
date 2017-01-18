package com.yoloo.backend.bookmark;

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
import static org.junit.Assert.assertEquals;

public class BookmarkControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Question question;

  private QuestionController questionController;
  private BookmarkController bookmarkController;
  private TagController tagController;
  private TopicController topicController;

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
    bookmarkController = BookmarkControllerFactory.of().create();
    tagController = TagControllerFactory.of().create();
    topicController = TopicControllerFactory.of().create();

    AccountModel model = createAccount();

    Account owner = model.getAccount();
    DeviceRecord record = createRecord(owner);
    Tracker tracker = GamificationService.create().create(owner.getKey());

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    Topic europe = topicController.add("europe", Topic.Type.THEME, user);

    TagGroup passport = tagController.addGroup("passport", user);

    Tag visa = tagController.addTag("visa", "en", passport.getWebsafeId(), user);

    ImmutableList<Object> saveList = ImmutableList.builder()
        .add(owner)
        .addAll(model.getShards())
        .add(tracker)
        .add(europe)
        .add(passport)
        .add(visa)
        .add(record)
        .build();

    ofy().save().entities(saveList).now();

    QuestionWrapper wrapper = createQuestionWrapper();

    question = questionController.add(wrapper, user);
  }

  @Test
  public void testSaveQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    bookmarkController.add(question.getWebsafeId(), user);

    List<Bookmark> bookmarks = ofy().load().type(Bookmark.class)
        .ancestor(Key.<Account>create(user.getUserId()))
        .list();

    assertEquals(1, bookmarks.size());
  }

  @Test
  public void testUnSaveQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    bookmarkController.add(question.getWebsafeId(), user);

    List<Bookmark> bookmarks1 = ofy().load().type(Bookmark.class)
        .ancestor(Key.<Account>create(user.getUserId()))
        .list();

    assertEquals(1, bookmarks1.size());

    bookmarkController.delete(question.getWebsafeId(), user);

    List<Bookmark> bookmarks2 = ofy().load().type(Bookmark.class)
        .ancestor(Key.<Account>create(user.getUserId()))
        .list();

    assertEquals(0, bookmarks2.size());
  }

  @Test
  public void testListSavedQuestions() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    QuestionWrapper wrapper2 = QuestionWrapper.builder()
        .content("hello2")
        .tags("visa")
        .topics("europe")
        .bounty(0)
        .build();

    Question question2 = questionController.add(wrapper2, user);

    bookmarkController.add(question.getWebsafeId(), user);
    bookmarkController.add(question2.getWebsafeId(), user);

    List<Bookmark> bookmarks = ofy().load().type(Bookmark.class)
        .ancestor(Key.<Account>create(user.getUserId()))
        .list();

    assertEquals(2, bookmarks.size());
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

  private QuestionWrapper createQuestionWrapper() {
    return QuestionWrapper.builder()
        .content("Test content")
        .tags("visa,passport")
        .topics("europe")
        .bounty(0)
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
