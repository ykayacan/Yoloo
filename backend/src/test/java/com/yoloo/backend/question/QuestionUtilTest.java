package com.yoloo.backend.question;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountModel;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.comment.CommentController;
import com.yoloo.backend.comment.CommentControllerFactory;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagController;
import com.yoloo.backend.tag.TagControllerFactory;
import com.yoloo.backend.tag.TagGroup;
import com.yoloo.backend.topic.Topic;
import com.yoloo.backend.topic.TopicController;
import com.yoloo.backend.topic.TopicControllerFactory;
import com.yoloo.backend.util.TestBase;
import com.yoloo.backend.util.TestObjectifyService;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteController;
import com.yoloo.backend.vote.VoteControllerFactory;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QuestionUtilTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Account owner;
  private Question question;

  private QuestionController questionController;
  private CommentController commentController;
  private TagController tagController;
  private TopicController topicController;
  private VoteController voteController;

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
    commentController = CommentControllerFactory.of().create();
    tagController = TagControllerFactory.of().create();
    topicController = TopicControllerFactory.of().create();
    voteController = VoteControllerFactory.of().create();

    AccountModel model = createAccount();

    owner = model.getAccount();
    DeviceRecord record = createRecord(owner);
    Tracker tracker = GamificationService.create().create(owner.getKey());

    ImmutableList<Object> saveList = ImmutableList.builder()
        .add(owner)
        .addAll(model.getShards())
        .add(record)
        .add(tracker)
        .build();

    TestObjectifyService.ofy().save().entities(saveList).now();

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    try {
      Topic europe = topicController.add("europe", Topic.Type.THEME, user);
    } catch (ConflictException e) {
      e.printStackTrace();
    }

    TagGroup passport = tagController.addGroup("passport", user);

    Tag visa = tagController.addTag("visa", "en", passport.getWebsafeId(), user);

    question = questionController.add("Test content", "visa,passport", "europe", Optional.absent(),
        Optional.absent(), user);
  }

  @Test
  public void testMergeCountsSingleQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    voteController.vote(question.getWebsafeId(), Vote.Direction.UP, user);
    commentController.add(question.getWebsafeId(), "Hello", Optional.absent(), user);

    Question merged = QuestionUtil.mergeCounts(question).blockingSingle();

    assertNotNull(merged);
    assertEquals(1, merged.getComments());
    assertEquals(1, merged.getVotes());
  }

  @Test
  public void testMergeCountsMultipleQuestions() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    List<Question> questions = Lists.newArrayList();
    questions.add(question);

    voteController.vote(question.getWebsafeId(), Vote.Direction.UP, user);
    commentController.add(question.getWebsafeId(), "Hello", Optional.absent(), user);

    Question question2 =
        questionController.add("Test content", "visa,passport", "europe", Optional.absent(),
            Optional.absent(), user);
    questions.add(question2);

    voteController.vote(question2.getWebsafeId(), Vote.Direction.UP, user);
    commentController.add(question2.getWebsafeId(), "Hello", Optional.absent(), user);

    Question question3 =
        questionController.add("Test content", "visa,passport", "europe", Optional.absent(),
            Optional.absent(), user);
    questions.add(question3);

    voteController.vote(question3.getWebsafeId(), Vote.Direction.UP, user);
    commentController.add(question3.getWebsafeId(), "Hello", Optional.absent(), user);

    QuestionUtil.mergeCounts(questions)
        .forEach(question1 -> {
          assertNotNull(question1);
          assertEquals(1, question1.getComments());
          assertEquals(1, question1.getVotes());
        });
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
