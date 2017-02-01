package com.yoloo.backend.question;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountModel;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.follow.FollowContollerFactory;
import com.yoloo.backend.follow.FollowController;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagController;
import com.yoloo.backend.tag.TagControllerFactory;
import com.yoloo.backend.topic.Topic;
import com.yoloo.backend.topic.TopicController;
import com.yoloo.backend.topic.TopicControllerFactory;
import com.yoloo.backend.util.TestBase;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteController;
import com.yoloo.backend.vote.VoteControllerFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QuestionControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Account owner;
  private Account owner2;

  private Topic budgetTravel;
  private Topic europe;

  private Tag passport;

  private Tag visa;
  private Tag visa2;

  private QuestionController questionController;
  private FollowController followController;
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
    followController = FollowContollerFactory.of().create();
    tagController = TagControllerFactory.of().create();
    topicController = TopicControllerFactory.of().create();
    voteController = VoteControllerFactory.of().create();

    AccountModel model1 = createAccount();
    AccountModel model2 = createAccount();

    owner = model1.getAccount();
    owner2 = model2.getAccount();

    DeviceRecord record1 = createRecord(owner);
    DeviceRecord record2 = createRecord(owner2);

    Tracker tracker = GamificationService.create().create(owner.getKey());

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(owner)
        .add(owner2)
        .add(record1)
        .add(record2)
        .addAll(model1.getShards())
        .addAll(model2.getShards())
        .add(tracker)
        .build();

    ofy().save().entities(saveList).now();

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    followController.follow(owner2.getWebsafeId(), user);

    try {
      budgetTravel = topicController.add("budget travel", Topic.Type.THEME, user);
    } catch (ConflictException e) {
      e.printStackTrace();
    }
    try {
      europe = topicController.add("europe", Topic.Type.THEME, user);
    } catch (ConflictException e) {
      e.printStackTrace();
    }

    passport = tagController.addGroup("passport", user);

    visa = tagController.addTag("visa", "en", passport.getWebsafeId(), user);
    visa2 = tagController.addTag("visa2", "en", passport.getWebsafeId(), user);
  }

  @Test
  public void testGetQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Question original =
        questionController.add("Test content", "visa,passport", "europe", Optional.absent(),
            Optional.of(10), user);

    Question fetched = questionController.get(original.getWebsafeId(), user);

    assertNotNull(fetched.getKey());
    assertEquals("Test content", fetched.getContent());
    assertEquals(0, fetched.getBounty());
    assertEquals(null, fetched.getAcceptedCommentKey());
    assertEquals(false, fetched.isCommented());
    assertEquals("Test user", fetched.getUsername());
    assertEquals(new Link("Test avatar"), fetched.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, fetched.getDir());

    Set<String> tags = ImmutableSet.<String>builder()
        .add("visa").add("passport").build();
    assertEquals(tags, fetched.getTags());

    Set<String> categories = ImmutableSet.<String>builder()
        .add("europe").build();
    assertEquals(categories, fetched.getCategories());

    assertEquals(0, fetched.getComments());
    assertEquals(0, fetched.getReports());
    assertEquals(0, fetched.getVotes());
  }

  @Test
  public void testGetQuestion_withVote() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Question original =
        questionController.add("Test content", "visa,passport", "europe", Optional.absent(),
            Optional.of(10), user);
    voteController.vote(original.getWebsafeId(), Vote.Direction.UP, user);

    Question fetched = questionController.get(original.getWebsafeId(), user);

    assertNotNull(fetched.getKey());
    assertEquals("Test content", fetched.getContent());
    assertEquals(0, fetched.getBounty());
    assertEquals(null, fetched.getAcceptedCommentKey());
    assertEquals(false, fetched.isCommented());
    assertEquals("Test user", fetched.getUsername());
    assertEquals(new Link("Test avatar"), fetched.getAvatarUrl());
    assertEquals(Vote.Direction.UP, fetched.getDir());

    Set<String> tags = ImmutableSet.<String>builder()
        .add("visa").add("passport").build();
    assertEquals(tags, fetched.getTags());

    Set<String> categories = ImmutableSet.<String>builder()
        .add("europe").build();
    assertEquals(categories, fetched.getCategories());

    assertEquals(0, fetched.getComments());
    assertEquals(0, fetched.getReports());
    assertEquals(1, fetched.getVotes());
  }

  @Test
  public void testAddQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories = europe.getName() + "," + budgetTravel.getName();

    Question question = questionController.add("Test content", tags, categories, Optional.absent(),
        Optional.of(10), user);

    assertNotNull(question.getKey());
    assertEquals("Test content", question.getContent());
    assertEquals(0, question.getBounty());

    Set<String> tagSet = new HashSet<>();
    tagSet.add(visa.getName());
    tagSet.add(visa2.getName());
    assertEquals(tagSet, question.getTags());

    assertEquals(null, question.getAcceptedCommentKey());
    assertEquals(false, question.isCommented());
    assertEquals("Test user", question.getUsername());
    assertEquals(new Link("Test avatar"), question.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, question.getDir());

    Set<String> categorySet = new HashSet<>();
    categorySet.add(budgetTravel.getName());
    categorySet.add(europe.getName());
    assertEquals(categorySet, question.getCategories());

    Tracker tracker = ofy().load().key(Tracker.createKey(Key.create(user.getUserId()))).now();

    assertNotNull(tracker);
    //assertEquals(120, tracker.getPoints());
    //assertEquals(2, tracker.getBounties());
  }

  @Test
  public void testAddQuestion_withMedia() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories = europe.getName() + "," + budgetTravel.getName();

    Media media = Media.builder()
        .id("bucket/test_item")
        .parentAccountKey(owner.getKey())
        .mime(MediaType.ANY_IMAGE_TYPE.toString())
        .url("test url")
        .build();

    Key<Media> mediaKey = ofy().save().entity(media).now();

    Question question = questionController.add("Test content", tags, categories,
        Optional.of(mediaKey.toWebSafeString()), Optional.of(10), user);

    assertNotNull(question.getKey());
    assertEquals("Test content", question.getContent());
    assertEquals(0, question.getBounty());

    Set<String> tagSet = new HashSet<>();
    tagSet.add(visa.getName());
    tagSet.add(visa2.getName());
    assertEquals(tagSet, question.getTags());

    assertEquals(null, question.getAcceptedCommentKey());
    assertEquals(false, question.isCommented());
    assertEquals("Test user", question.getUsername());
    assertEquals(new Link("Test avatar"), question.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, question.getDir());
    assertEquals(media.getUrl(), question.getMedia().getUrl());
    assertEquals(media.getId(), question.getMedia().getId());

    Set<String> categorySet = new HashSet<>();
    categorySet.add(budgetTravel.getName());
    categorySet.add(europe.getName());
    assertEquals(categorySet, question.getCategories());
  }

  @Test
  public void testListQuestions_category() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories1 = europe.getName() + "," + budgetTravel.getName();

    questionController.add("Test content", tags, categories1, Optional.absent(), Optional.of(10),
        user);

    String categories2 = europe.getName();

    questionController.add("Test content", tags, categories2, Optional.absent(), Optional.of(10),
        user);

    CollectionResponse<Question> response = questionController.list(
        Optional.absent(),
        Optional.fromNullable(budgetTravel.getName()),
        Optional.absent(),
        Optional.absent(),
        Optional.absent(),
        user);

    assertNotNull(response.getItems());
    assertEquals(1, response.getItems().size());
    for (Question question : response.getItems()) {
      assertEquals("Test content", question.getContent());
      assertEquals("[europe, budget travel]", question.getCategories().toString());
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

  private DeviceRecord createRecord(Account owner) {
    return DeviceRecord.builder()
        .id(owner.getWebsafeId())
        .parentUserKey(owner.getKey())
        .regId(UUID.randomUUID().toString())
        .build();
  }
}