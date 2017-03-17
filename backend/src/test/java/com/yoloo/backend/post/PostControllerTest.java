package com.yoloo.backend.post;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
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
import com.yoloo.backend.account.AccountEntity;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.category.CategoryController;
import com.yoloo.backend.category.CategoryControllerFactory;
import com.yoloo.backend.comment.CommentController;
import com.yoloo.backend.comment.CommentControllerFactory;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.follow.FollowController;
import com.yoloo.backend.follow.FollowControllerFactory;
import com.yoloo.backend.game.GamificationService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.media.Media;
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

  private Category budgetTravel;
  private Category europe;
  private Category america;

  private Tag visa;
  private Tag visa2;

  private PostController postController;
  private VoteController voteController;
  private CommentController commentController;

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
    FollowController followController = FollowControllerFactory.of().create();
    TagController tagController = TagControllerFactory.of().create();
    CategoryController categoryController = CategoryControllerFactory.of().create();
    voteController = VoteControllerFactory.of().create();
    commentController = CommentControllerFactory.of().create();

    AccountEntity model1 = createAccount();
    AccountEntity model2 = createAccount();

    owner = model1.getAccount();
    Account owner2 = model2.getAccount();

    DeviceRecord record1 = createRecord(owner);
    DeviceRecord record2 = createRecord(owner2);

    Tracker tracker = GamificationService.create().createTracker(owner.getKey());

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(owner)
        .add(owner2)
        .add(record1)
        .add(record2)
        .addAll(model1.getShards().values())
        .addAll(model2.getShards().values())
        .add(tracker)
        .build();

    ofy().save().entities(saveList).now();

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    followController.follow(owner2.getWebsafeId(), user);

    try {
      budgetTravel = categoryController.insertCategory("budget travel", null);
    } catch (ConflictException e) {
      e.printStackTrace();
    }
    try {
      europe = categoryController.insertCategory("europe", null);
    } catch (ConflictException e) {
      e.printStackTrace();
    }
    try {
      america = categoryController.insertCategory("america", null);
    } catch (ConflictException e) {
      e.printStackTrace();
    }

    Tag passport = tagController.insertGroup("passport");

    visa = tagController.insertTag("visa", "en", passport.getWebsafeId());
    visa2 = tagController.insertTag("visa2", "en", passport.getWebsafeId());
  }

  @Test
  public void testGetQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Post original =
        postController.insertQuestion("Test content", "visa,passport", europe.getWebsafeId(),
            Optional.absent(),
            Optional.of(10), user);

    Post fetched = postController.getPost(original.getWebsafeId(), user);

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
    assertEquals(categories, fetched.getCategories());

    assertEquals(0, fetched.getCommentCount());
    assertEquals(0, fetched.getReportCount());
    assertEquals(0, fetched.getVoteCount());
  }

  @Test
  public void testGetQuestion_withVote() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Post original =
        postController.insertQuestion("Test content", "visa,passport", europe.getWebsafeId(),
            Optional.absent(),
            Optional.of(10), user);
    voteController.vote(original.getWebsafeId(), Vote.Direction.UP, user);

    Post fetched = postController.getPost(original.getWebsafeId(), user);

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

    assertEquals(0, fetched.getCommentCount());
    assertEquals(0, fetched.getReportCount());
    assertEquals(1, fetched.getVoteCount());
  }

  @Test
  public void testAddQuestion() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories = europe.getWebsafeId() + "," + budgetTravel.getWebsafeId();

    Post post = postController.insertQuestion("Test content", tags, categories, Optional.absent(),
        Optional.of(10), user);

    assertNotNull(post.getKey());
    assertEquals("Test content", post.getContent());
    assertEquals(0, post.getBounty());

    Set<String> tagSet = new HashSet<>();
    tagSet.add(visa.getName());
    tagSet.add(visa2.getName());
    assertEquals(tagSet, post.getTags());

    assertEquals(null, post.getAcceptedCommentKey());
    assertEquals(false, post.isCommented());
    assertEquals("Test user", post.getUsername());
    assertEquals(new Link("Test avatar"), post.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, post.getDir());

    Set<String> categorySet = new HashSet<>();
    categorySet.add(budgetTravel.getName());
    categorySet.add(europe.getName());
    assertEquals(categorySet, post.getCategories());

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

    Media media = Media.builder()
        .id("bucket/test_item")
        .parent(owner.getKey())
        .mime(MediaType.ANY_IMAGE_TYPE.toString())
        .url("test url")
        .build();

    Key<Media> mediaKey = ofy().save().entity(media).now();

    Post post = postController.insertQuestion("Test content", tags, categories,
        Optional.of(mediaKey.toWebSafeString()), Optional.of(10), user);

    assertNotNull(post.getKey());
    assertEquals("Test content", post.getContent());
    assertEquals(0, post.getBounty());

    Set<String> tagSet = new HashSet<>();
    tagSet.add(visa.getName());
    tagSet.add(visa2.getName());
    assertEquals(tagSet, post.getTags());

    assertEquals(null, post.getAcceptedCommentKey());
    assertEquals(false, post.isCommented());
    assertEquals("Test user", post.getUsername());
    assertEquals(new Link("Test avatar"), post.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, post.getDir());
    assertEquals(media.getUrl(), post.getMedia().getUrl());
    assertEquals(media.getId(), post.getMedia().getId());

    Set<String> categorySet = new HashSet<>();
    categorySet.add(budgetTravel.getName());
    categorySet.add(europe.getName());
    assertEquals(categorySet, post.getCategories());
  }

  @Test
  public void testListQuestions() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categoryIds = europe.getWebsafeId()
        + ","
        + budgetTravel.getWebsafeId()
        + ","
        + america.getWebsafeId();

    postController.insertQuestion("Test content", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);
    postController.insertQuestion("Test content", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);

    CollectionResponse<Post> response = postController.listPosts(
        Optional.absent(),
        Optional.absent(),
        Optional.fromNullable(budgetTravel.getName()),
        Optional.absent(),
        Optional.absent(),
        Optional.absent(),
        Optional.of(Post.PostType.QUESTION),
        user);

    assertNotNull(response.getItems());
    assertEquals(2, response.getItems().size());
    for (Post post : response.getItems()) {
      assertEquals("Test content", post.getContent());
      assertEquals("[budget travel, europe, america]", post.getCategories().toString());
    }
  }

  @Test
  public void testListQuestions_category() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories1 = europe.getWebsafeId() + "," + budgetTravel.getWebsafeId();

    postController.insertQuestion("Test content", tags, categories1, Optional.absent(),
        Optional.of(10), user);

    String categories2 = europe.getWebsafeId();

    postController.insertQuestion("Test content", tags, categories2, Optional.absent(),
        Optional.of(10), user);

    CollectionResponse<Post> response = postController.listPosts(
        Optional.absent(),
        Optional.absent(),
        Optional.fromNullable(budgetTravel.getName()),
        Optional.absent(),
        Optional.absent(),
        Optional.absent(),
        Optional.of(Post.PostType.QUESTION),
        user);

    assertNotNull(response.getItems());
    assertEquals(1, response.getItems().size());
    for (Post post : response.getItems()) {
      assertEquals("Test content", post.getContent());
      assertEquals("[budget travel, europe]", post.getCategories().toString());
    }
  }

  @Test
  public void testListQuestions_voteAndComments() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categoryIds = europe.getWebsafeId() + "," + budgetTravel.getWebsafeId();

    Post p1 = postController.insertQuestion("1. post", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);
    commentController.insertComment(p1.getWebsafeId(), "Test comment", user);
    voteController.vote(p1.getWebsafeId(), Vote.Direction.UP, user);

    Post p2 = postController.insertQuestion("2. post", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);

    Post p3 = postController.insertQuestion("3. post", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);
    commentController.insertComment(p3.getWebsafeId(), "Test comment", user);
    voteController.vote(p3.getWebsafeId(), Vote.Direction.DOWN, user);

    Post p4 = postController.insertQuestion("4. post", tags, categoryIds, Optional.absent(),
        Optional.absent(), user);
    commentController.insertComment(p4.getWebsafeId(), "Test comment", user);
    commentController.insertComment(p4.getWebsafeId(), "Test comment 2", user);

    CollectionResponse<Post> response = postController.listPosts(
        Optional.absent(),
        Optional.absent(),
        Optional.fromNullable(budgetTravel.getName()),
        Optional.absent(),
        Optional.absent(),
        Optional.absent(),
        Optional.of(Post.PostType.QUESTION),
        user);

    assertNotNull(response.getItems());
    assertEquals(4, response.getItems().size());

    for (Post post : response.getItems()) {
      if (post.getKey().equivalent(p1.getKey())) {
        assertEquals(1, post.getCommentCount());
        assertEquals(1, post.getVoteCount());
        assertEquals(Vote.Direction.UP, post.getDir());
      } else if (post.getKey().equivalent(p2.getKey())) {
        assertEquals(0, post.getCommentCount());
        assertEquals(0, post.getVoteCount());
        assertEquals(Vote.Direction.DEFAULT, post.getDir());
      } else if (post.getKey().equivalent(p3.getKey())) {
        assertEquals(1, post.getCommentCount());
        assertEquals(-1, post.getVoteCount());
        assertEquals(Vote.Direction.DOWN, post.getDir());
      } else if (post.getKey().equivalent(p4.getKey())) {
        assertEquals(2, post.getCommentCount());
        assertEquals(0, post.getVoteCount());
        assertEquals(Vote.Direction.DEFAULT, post.getDir());
      }
    }
  }

  private AccountEntity createAccount() {
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

    return AccountEntity.builder()
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