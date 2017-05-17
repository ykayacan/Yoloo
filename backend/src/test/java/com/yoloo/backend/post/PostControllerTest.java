package com.yoloo.backend.post;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.comment.CommentController;
import com.yoloo.backend.comment.CommentControllerFactory;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.group.TravelerGroupController;
import com.yoloo.backend.group.TravelerGroupControllerFactory;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.relationship.RelationshipController;
import com.yoloo.backend.relationship.RelationshipControllerFactory;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagController;
import com.yoloo.backend.tag.TagControllerFactory;
import com.yoloo.backend.util.TestBase;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteController;
import com.yoloo.backend.vote.VoteControllerFactory;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PostControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Account owner;

  private TravelerGroupEntity budgetTravel;
  private TravelerGroupEntity europe;
  private TravelerGroupEntity america;

  private Tag visa;
  private Tag visa2;

  private PostController postController;
  private VoteController voteController;
  private CommentController commentController;

  @Override
  public void setUpGAE() {
    super.setUpGAE();

    helper
        .setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);
  }

  @Override
  public void setUp() {
    super.setUp();

    postController = PostControllerFactory.of().create();
    RelationshipController relationshipController = RelationshipControllerFactory.of().create();
    TagController tagController = TagControllerFactory.of().create();
    TravelerGroupController travelerGroupController = TravelerGroupControllerFactory.of().create();
    voteController = VoteControllerFactory.of().create();
    commentController = CommentControllerFactory.of().create();

    owner = createAccount();
    Account owner2 = createAccount();

    DeviceRecord record1 = createRecord(owner);
    DeviceRecord record2 = createRecord(owner2);

    Tracker tracker = GameService.create().createTracker(owner.getKey());

    ImmutableSet<Object> saveList = ImmutableSet
        .builder()
        .add(owner)
        .add(owner2)
        .add(record1)
        .add(record2)
        .addAll(owner.getShardMap().values())
        .addAll(owner2.getShardMap().values())
        .add(tracker)
        .build();

    ofy().save().entities(saveList).now();

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    relationshipController.follow(owner2.getWebsafeId(), user);

    budgetTravel = travelerGroupController.insertGroup("budget travel", null);
    europe = travelerGroupController.insertGroup("europe", null);
    america = travelerGroupController.insertGroup("america", null);

    Tag passport = tagController.insertTag("passport");
    visa = tagController.insertTag("visa");
    visa2 = tagController.insertTag("visa2");
  }

  @Test
  public void testGetQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    PostEntity original =
        postController.insertQuestionPost("Test content", "visa,passport", europe.getWebsafeId(),
            Optional.absent(), Optional.of(10), user);

    PostEntity fetched = postController.getPost(original.getWebsafeId(), user);

    assertNotNull(fetched.getKey());
    assertEquals("Test content", fetched.getContent());
    assertEquals(0, fetched.getBounty());
    assertEquals(null, fetched.getAcceptedCommentKey());
    assertEquals(false, fetched.isCommented());
    assertEquals("Test user", fetched.getUsername());
    assertEquals(new Link("Test avatar"), fetched.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, fetched.getDir());

    Set<String> tags = ImmutableSet.<String>builder().add("visa").add("passport").build();
    assertEquals(tags, fetched.getTags());

    Set<String> categories = ImmutableSet.<String>builder().add("europe").build();
    //assertEquals(categories, fetched.getTravelerGroup());

    assertEquals(0, fetched.getCommentCount());
    assertEquals(0, fetched.getReportCount());
    assertEquals(0, fetched.getVoteCount());
  }

  @Test
  public void testGetQuestion_withVote() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    PostEntity original =
        postController.insertQuestionPost("Test content", "visa,passport", europe.getWebsafeId(),
            Optional.absent(), Optional.of(10), user);
    voteController.votePost(original.getWebsafeId(), Vote.Direction.UP.getValue(), user);

    PostEntity fetched = postController.getPost(original.getWebsafeId(), user);

    assertNotNull(fetched.getKey());
    assertEquals("Test content", fetched.getContent());
    assertEquals(0, fetched.getBounty());
    assertEquals(null, fetched.getAcceptedCommentKey());
    assertEquals(false, fetched.isCommented());
    assertEquals("Test user", fetched.getUsername());
    assertEquals(new Link("Test avatar"), fetched.getAvatarUrl());
    assertEquals(Vote.Direction.UP, fetched.getDir());

    Set<String> tags = ImmutableSet.<String>builder().add("visa").add("passport").build();
    assertEquals(tags, fetched.getTags());

    Set<String> categories = ImmutableSet.<String>builder().add("europe").build();
    //assertEquals(categories, fetched.getCategories());

    assertEquals(0, fetched.getCommentCount());
    assertEquals(0, fetched.getReportCount());
    assertEquals(1, fetched.getVoteCount());
  }

  @Test
  public void testAddQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories = europe.getWebsafeId() + "," + budgetTravel.getWebsafeId();

    PostEntity postEntity = postController.insertQuestionPost("Test content", tags, categories, Optional.absent(),
        Optional.of(10), user);

    assertNotNull(postEntity.getKey());
    assertEquals("Test content", postEntity.getContent());
    assertEquals(0, postEntity.getBounty());

    Set<String> tagSet = new HashSet<>();
    tagSet.add(visa.getName());
    tagSet.add(visa2.getName());
    assertEquals(tagSet, postEntity.getTags());

    assertEquals(null, postEntity.getAcceptedCommentKey());
    assertEquals(false, postEntity.isCommented());
    assertEquals("Test user", postEntity.getUsername());
    assertEquals(new Link("Test avatar"), postEntity.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, postEntity.getDir());

    Set<String> categorySet = new HashSet<>();
    categorySet.add(budgetTravel.getName());
    categorySet.add(europe.getName());
    //assertEquals(categorySet, post.getCategories());

    Tracker tracker = ofy().load().key(Tracker.createKey(Key.create(user.getUserId()))).now();

    assertNotNull(tracker);
    assertEquals(120, tracker.getPoints());
    assertEquals(2, tracker.getBounties());
  }

  @Test
  public void testAddQuestion_withMedia() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories = europe.getWebsafeId() + "," + budgetTravel.getWebsafeId();

    MediaEntity mediaEntity = MediaEntity
        .builder()
        .id("bucket/test_item")
        .parent(owner.getKey())
        .mime(MediaType.ANY_IMAGE_TYPE.toString())
        .url("test url")
        .build();

    Key<MediaEntity> mediaKey = ofy().save().entity(mediaEntity).now();

    PostEntity postEntity = postController.insertQuestionPost("Test content", tags, categories,
        Optional.of(mediaKey.toWebSafeString()), Optional.of(10), user);

    assertNotNull(postEntity.getKey());
    assertEquals("Test content", postEntity.getContent());
    assertEquals(0, postEntity.getBounty());

    Set<String> tagSet = new HashSet<>();
    tagSet.add(visa.getName());
    tagSet.add(visa2.getName());
    assertEquals(tagSet, postEntity.getTags());

    assertEquals(null, postEntity.getAcceptedCommentKey());
    assertEquals(false, postEntity.isCommented());
    assertEquals("Test user", postEntity.getUsername());
    assertEquals(new Link("Test avatar"), postEntity.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, postEntity.getDir());
    //assertEquals(mediaEntity.getUrl(), post.getMedia().getUrl());
    //assertEquals(mediaEntity.getId(), post.getMedia().getId());

    Set<String> categorySet = new HashSet<>();
    categorySet.add(budgetTravel.getName());
    categorySet.add(europe.getName());
    //assertEquals(categorySet, post.getCategories());
  }

  @Test
  public void testListQuestions() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categoryIds =
        europe.getWebsafeId() + "," + budgetTravel.getWebsafeId() + "," + america.getWebsafeId();

    postController.insertQuestionPost("Test content", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);
    postController.insertQuestionPost("Test content", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);

    CollectionResponse<PostEntity> response =
        postController.listPosts(Optional.absent(), Optional.absent(),
            Optional.fromNullable(budgetTravel.getName()), Optional.absent(), Optional.absent(),
            Optional.absent(), Optional.of(PostEntity.Type.TEXT_POST), user);

    assertNotNull(response.getItems());
    assertEquals(2, response.getItems().size());
    for (PostEntity postEntity : response.getItems()) {
      assertEquals("Test content", postEntity.getContent());
      //assertEquals("[budget travel, europe, america]", post.getCategories().toString());
    }
  }

  @Test
  public void testListQuestions_category() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories1 = europe.getWebsafeId() + "," + budgetTravel.getWebsafeId();

    postController.insertQuestionPost("Test content", tags, categories1, Optional.absent(),
        Optional.of(10), user);

    String categories2 = europe.getWebsafeId();

    postController.insertQuestionPost("Test content", tags, categories2, Optional.absent(),
        Optional.of(10), user);

    CollectionResponse<PostEntity> response =
        postController.listPosts(Optional.absent(), Optional.absent(),
            Optional.fromNullable(budgetTravel.getName()), Optional.absent(), Optional.absent(),
            Optional.absent(), Optional.of(PostEntity.Type.TEXT_POST), user);

    assertNotNull(response.getItems());
    assertEquals(1, response.getItems().size());
    for (PostEntity postEntity : response.getItems()) {
      assertEquals("Test content", postEntity.getContent());
      //assertEquals("[budget travel, europe]", post.getCategories().toString());
    }
  }

  @Test
  public void testListQuestions_voteAndComments() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categoryIds = europe.getWebsafeId() + "," + budgetTravel.getWebsafeId();

    PostEntity p1 = postController.insertQuestionPost("1. post", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);
    commentController.insertComment(p1.getWebsafeId(), "Test comment", user);
    voteController.votePost(p1.getWebsafeId(), Vote.Direction.UP.getValue(), user);

    PostEntity p2 = postController.insertQuestionPost("2. post", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);

    PostEntity p3 = postController.insertQuestionPost("3. post", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);
    commentController.insertComment(p3.getWebsafeId(), "Test comment", user);
    voteController.votePost(p3.getWebsafeId(), Vote.Direction.DOWN.getValue(), user);

    PostEntity p4 = postController.insertQuestionPost("4. post", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);
    commentController.insertComment(p4.getWebsafeId(), "Test comment", user);
    commentController.insertComment(p4.getWebsafeId(), "Test comment 2", user);

    CollectionResponse<PostEntity> response =
        postController.listPosts(Optional.absent(), Optional.absent(),
            Optional.fromNullable(budgetTravel.getName()), Optional.absent(), Optional.absent(),
            Optional.absent(), Optional.of(PostEntity.Type.TEXT_POST), user);

    assertNotNull(response.getItems());
    assertEquals(4, response.getItems().size());

    for (PostEntity postEntity : response.getItems()) {
      if (postEntity.getKey().equivalent(p1.getKey())) {
        assertEquals(1, postEntity.getCommentCount());
        assertEquals(1, postEntity.getVoteCount());
        assertEquals(Vote.Direction.UP, postEntity.getDir());
      } else if (postEntity.getKey().equivalent(p2.getKey())) {
        assertEquals(0, postEntity.getCommentCount());
        assertEquals(0, postEntity.getVoteCount());
        assertEquals(Vote.Direction.DEFAULT, postEntity.getDir());
      } else if (postEntity.getKey().equivalent(p3.getKey())) {
        assertEquals(1, postEntity.getCommentCount());
        assertEquals(-1, postEntity.getVoteCount());
        assertEquals(Vote.Direction.DOWN, postEntity.getDir());
      } else if (postEntity.getKey().equivalent(p4.getKey())) {
        assertEquals(2, postEntity.getCommentCount());
        assertEquals(0, postEntity.getVoteCount());
        assertEquals(Vote.Direction.DEFAULT, postEntity.getDir());
      }
    }
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

  private DeviceRecord createRecord(Account owner) {
    return DeviceRecord
        .builder()
        .id(owner.getWebsafeId())
        .parent(owner.getKey())
        .regId(UUID.randomUUID().toString())
        .build();
  }
}