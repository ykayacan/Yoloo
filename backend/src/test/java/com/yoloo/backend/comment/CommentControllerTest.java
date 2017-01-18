package com.yoloo.backend.comment;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
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
import com.yoloo.backend.question.QuestionUtil;
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
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteController;
import com.yoloo.backend.vote.VoteControllerFactory;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommentControllerTest extends TestBase {

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

    ofy().save().entities(saveList).now();

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    Topic europe = topicController.add("europe", Topic.Type.THEME, user);

    TagGroup passport = tagController.addGroup("passport", user);

    Tag visa = tagController.addTag("visa", "en", passport.getWebsafeId(), user);

    question = questionController.add(createQuestionWrapper(), user);
  }

  @Test
  public void testAddComment() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String content = "Test comment";

    Comment comment =
        commentController.add(question.getWebsafeId(), content, Optional.absent(), user);

    assertEquals(content, comment.getContent());
    assertEquals(Key.create(user.getUserId()), comment.getParentUserKey());
    assertEquals(owner.getUsername(), comment.getUsername());
    assertEquals(owner.getAvatarUrl(), comment.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0, comment.getVotes());
    assertEquals(false, comment.isAccepted());

    Question question = ofy().load().group(Question.ShardGroup.class)
        .key(comment.getQuestionKey()).now();
    question = QuestionUtil.mergeCounts(question).blockingSingle();

    assertEquals(1, question.getComments());
    assertEquals(true, question.isCommented());

    Tracker tracker = ofy().load().key(Tracker.createKey(Key.create(user.getUserId()))).now();

    assertNotNull(tracker);
    //assertEquals(240, tracker.getPoints());
    //assertEquals(3, tracker.getBounties());
    assertEquals(true, tracker.isFirstComment());
  }

  @Test
  public void testAddComment_firstComment() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    final Key<Tracker> trackerKey = Tracker.createKey(Key.create(user.getUserId()));

    Comment comment =
        commentController.add(question.getWebsafeId(), "Test comment", Optional.absent(), user);

    Tracker tracker = ofy().load().key(trackerKey).now();
    Question question = ofy().load().key(comment.getQuestionKey()).now();

    assertEquals(true, question.isCommented());
    assertEquals(true, tracker.isFirstComment());
    //assertEquals(240, tracker.getPoints());
    //assertEquals(3, tracker.getBounties());
  }

  @Test
  public void testUpdateComment_contentChanged() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String originalContent = "Test comment";

    Comment original =
        commentController.add(question.getWebsafeId(), originalContent, Optional.absent(), user);

    assertEquals(originalContent, original.getContent());
    assertEquals(false, original.isAccepted());

    String changedContent = "Hello Yoloo";

    Comment changed = commentController.update(question.getWebsafeId(), original.getWebsafeId(),
        Optional.of(changedContent), Optional.absent(), user);

    assertEquals(changedContent, changed.getContent());
    assertEquals(false, changed.isAccepted());
  }

  @Test
  public void testUpdateComment_acceptedChanged() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String originalContent = "Test comment";

    Comment original =
        commentController.add(question.getWebsafeId(), originalContent, Optional.absent(), user);

    Comment changed = commentController.update(question.getWebsafeId(), original.getWebsafeId(),
        Optional.absent(), Optional.of(true), user);

    assertEquals(originalContent, changed.getContent());
    assertEquals(true, changed.isAccepted());

    Question question = ofy().load().key(changed.getQuestionKey()).now();
    assertEquals(changed.getKey(), question.getAcceptedCommentKey());
  }

  @Test(expected = NotFoundException.class)
  public void testRemoveComment() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String content = "Test comment";

    Comment comment =
        commentController.add(question.getWebsafeId(), content, Optional.absent(), user);

    commentController.delete(question.getWebsafeId(), comment.getWebsafeId(), user);

    ofy().load().key(comment.getKey()).safe();
  }

  @Test
  public void testRemoveComment_removeArtifacts() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String content = "Test comment";

    Comment comment =
        commentController.add(question.getWebsafeId(), content, Optional.absent(), user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.UP, user);

    commentController.delete(question.getWebsafeId(), comment.getWebsafeId(), user);

    assertEquals(null, ofy().load().key(comment.getKey()).now());
    assertEquals(true, ofy().load().refs(comment.getShardRefs()).isEmpty());

    List<Key<Vote>> voteKeys = ofy().load()
        .type(Vote.class)
        .filter(Vote.FIELD_VOTABLE_KEY + " =", comment.getKey())
        .keys()
        .list();

    assertEquals(0, voteKeys.size());
  }

  @Test
  public void testListComments() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Comment comment1 =
        commentController.add(question.getWebsafeId(), "Test comment1", Optional.absent(),
            user);
    Comment comment2 =
        commentController.add(question.getWebsafeId(), "Test comment2", Optional.absent(),
            user);
    Comment comment3 =
        commentController.add(question.getWebsafeId(), "Test comment3", Optional.absent(),
            user);
    Comment comment4 =
        commentController.add(question.getWebsafeId(), "Test comment4", Optional.absent(),
            user);

    voteController.vote(comment1.getWebsafeId(), Vote.Direction.UP, user);
    voteController.vote(comment2.getWebsafeId(), Vote.Direction.UP, user);
    voteController.vote(comment3.getWebsafeId(), Vote.Direction.UP, user);
    voteController.vote(comment4.getWebsafeId(), Vote.Direction.UP, user);

    CollectionResponse<Comment> response = commentController
        .list(question.getWebsafeId(), Optional.absent(), Optional.absent(), user);

    assertEquals(4, response.getItems().size());

    for (Comment comment : response.getItems()) {
      assertEquals(2, comment.getShards().size());
      assertEquals(1, comment.getVotes());
      assertEquals(Vote.Direction.UP, comment.getDir());
    }
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