package com.yoloo.backend.vote;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountModel;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentController;
import com.yoloo.backend.comment.CommentControllerFactory;
import com.yoloo.backend.comment.CommentUtil;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionController;
import com.yoloo.backend.question.QuestionControllerFactory;
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

public class VoteControllerTest extends TestBase {

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

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    Topic europe = null;
    try {
      europe = topicController.add("europe", Topic.Type.THEME, user);
    } catch (ConflictException e) {
      e.printStackTrace();
    }

    TagGroup passport = tagController.addGroup("passport", user);

    Tag visa = tagController.addTag("visa", "en", passport.getWebsafeId(), user);

    ImmutableList<Object> saveList = ImmutableList.builder()
        .add(owner)
        .addAll(model.getShards())
        .add(record)
        .add(tracker)
        .add(europe)
        .add(passport)
        .add(visa)
        .build();

    ofy().save().entities(saveList).now();

    question = questionController.add("Test content", "visa,passport", "europe", Optional.absent(),
        Optional.absent(), user);
  }

  @Test
  public void testVoteComment_Up() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Comment comment =
        commentController.add(question.getWebsafeId(), "Test comment", Optional.absent(), user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.UP, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.UP, comment.getDir());
    assertEquals(1, comment.getVotes());
  }

  @Test
  public void testVoteComment_Down() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Comment comment =
        commentController.add(question.getWebsafeId(), "Test comment", Optional.absent(), user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.DOWN, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.DOWN, comment.getDir());
    assertEquals(-1, comment.getVotes());
  }

  @Test
  public void testVoteComment_Default() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Comment comment =
        commentController.add(question.getWebsafeId(), "Test comment", Optional.absent(), user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0, comment.getVotes());
  }

  @Test
  public void testVoteComment_UpToDefault() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Comment comment =
        commentController.add(question.getWebsafeId(), "Test comment", Optional.absent(), user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.UP, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.UP, comment.getDir());
    assertEquals(1, comment.getVotes());

    voteController.vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0, comment.getVotes());
  }

  @Test
  public void testVoteComment_DefaultToUp() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Comment comment =
        commentController.add(question.getWebsafeId(), "Test comment", Optional.absent(), user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0, comment.getVotes());

    voteController.vote(comment.getWebsafeId(), Vote.Direction.UP, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.UP, comment.getDir());
    assertEquals(1, comment.getVotes());
  }

  @Test
  public void testVoteComment_DefaultToDown() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Comment comment =
        commentController.add(question.getWebsafeId(), "Test comment", Optional.absent(), user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0, comment.getVotes());

    voteController.vote(comment.getWebsafeId(), Vote.Direction.DOWN, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.DOWN, comment.getDir());
    assertEquals(-1, comment.getVotes());
  }

  @Test
  public void testVoteComment_DownToDefault() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Comment comment =
        commentController.add(question.getWebsafeId(), "Test comment", Optional.absent(), user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.DOWN, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.DOWN, comment.getDir());
    assertEquals(-1, comment.getVotes());

    voteController.vote(comment.getWebsafeId(), Vote.Direction.DEFAULT, user);

    comment = CommentUtil.mergeVoteDirection(comment, Key.create(user.getUserId()))
        .flatMap(CommentUtil::mergeCommentCounts)
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0, comment.getVotes());
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