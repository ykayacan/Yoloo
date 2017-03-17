package com.yoloo.backend.comment;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountEntity;
import com.yoloo.backend.account.AccountShard;
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
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagController;
import com.yoloo.backend.tag.TagControllerFactory;
import com.yoloo.backend.util.TestBase;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteController;
import com.yoloo.backend.vote.VoteControllerFactory;
import io.reactivex.Observable;
import java.util.List;
import java.util.Map;
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
  private Post post;

  private Category europe;

  private CommentController commentController;
  private VoteController voteController;

  private PostShardService postShardService;

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

    PostController postController = PostControllerFactory.of().create();
    commentController = CommentControllerFactory.of().create();
    TagController tagController = TagControllerFactory.of().create();
    CategoryController categoryController = CategoryControllerFactory.of().create();
    voteController = VoteControllerFactory.of().create();

    postShardService = PostShardService.create();

    AccountEntity model = createAccount();

    owner = model.getAccount();
    DeviceRecord record = createRecord(owner);
    Tracker tracker = GamificationService.create().createTracker(owner.getKey());

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(owner)
        .addAll(model.getShards().values())
        .add(record)
        .add(tracker)
        .build();

    ofy().save().entities(saveList).now();

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    try {
      europe = categoryController.insertCategory("europe", null);
    } catch (ConflictException e) {
      e.printStackTrace();
    }

    Tag passport = tagController.insertGroup("passport");
    tagController.insertTag("visa", "en", passport.getWebsafeId());

    post = postController.insertQuestion("Test content", "visa,passport", europe.getWebsafeId(),
        Optional.absent(), Optional.absent(), user);
  }

  @Test
  public void testAddComment() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String content = "Test comment";

    Comment comment = commentController.insertComment(this.post.getWebsafeId(), content, user);

    assertEquals(content, comment.getContent());
    assertEquals(Key.create(user.getUserId()), comment.getParent());
    assertEquals(owner.getUsername(), comment.getUsername());
    assertEquals(owner.getAvatarUrl(), comment.getAvatarUrl());
    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0, comment.getVoteCount());
    assertEquals(false, comment.isAccepted());

    Post post = Observable.just(
        ofy().load().group(Post.ShardGroup.class).key(comment.getPostKey()).now())
        .flatMap(postShardService::mergeShards)
        .blockingSingle();

    assertEquals(1, post.getCommentCount());
    assertEquals(true, post.isCommented());

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
        commentController.insertComment(post.getWebsafeId(), "Test comment", user);

    Tracker tracker = ofy().load().key(trackerKey).now();
    Post post = ofy().load().key(comment.getPostKey()).now();

    assertEquals(true, post.isCommented());
    assertEquals(true, tracker.isFirstComment());
    //assertEquals(240, tracker.getPoints());
    //assertEquals(3, tracker.getBounties());
  }

  @Test
  public void testUpdateComment_contentChanged() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String originalContent = "Test comment";

    Comment original =
        commentController.insertComment(post.getWebsafeId(), originalContent, user);

    assertEquals(originalContent, original.getContent());
    assertEquals(false, original.isAccepted());

    String changedContent = "Hello Yoloo";

    Comment changed = commentController.updateComment(post.getWebsafeId(), original.getWebsafeId(),
        Optional.of(changedContent), Optional.absent(), user);

    assertEquals(changedContent, changed.getContent());
    assertEquals(false, changed.isAccepted());
  }

  @Test
  public void testUpdateComment_acceptedChanged() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String originalContent = "Test comment";

    Comment original =
        commentController.insertComment(post.getWebsafeId(), originalContent, user);

    Comment changed = commentController.updateComment(post.getWebsafeId(), original.getWebsafeId(),
        Optional.absent(), Optional.of(true), user);

    assertEquals(originalContent, changed.getContent());
    assertEquals(true, changed.isAccepted());

    Post post = ofy().load().key(changed.getPostKey()).now();
    assertEquals(changed.getKey(), post.getAcceptedCommentKey());
  }

  @Test(expected = NotFoundException.class)
  public void testRemoveComment() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String content = "Test comment";

    Comment comment =
        commentController.insertComment(post.getWebsafeId(), content, user);

    commentController.deleteComment(post.getWebsafeId(), comment.getWebsafeId(), user);

    ofy().load().key(comment.getKey()).safe();
  }

  @Test
  public void testRemoveComment_removeArtifacts() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String content = "Test comment";

    Comment comment =
        commentController.insertComment(post.getWebsafeId(), content, user);

    voteController.vote(comment.getWebsafeId(), Vote.Direction.UP, user);

    commentController.deleteComment(post.getWebsafeId(), comment.getWebsafeId(), user);

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
        commentController.insertComment(post.getWebsafeId(), "Test comment1", user);
    Comment comment2 =
        commentController.insertComment(post.getWebsafeId(), "Test comment2", user);
    Comment comment3 =
        commentController.insertComment(post.getWebsafeId(), "Test comment3", user);
    Comment comment4 =
        commentController.insertComment(post.getWebsafeId(), "Test comment4", user);

    voteController.vote(comment1.getWebsafeId(), Vote.Direction.UP, user);
    voteController.vote(comment2.getWebsafeId(), Vote.Direction.UP, user);
    voteController.vote(comment3.getWebsafeId(), Vote.Direction.UP, user);
    voteController.vote(comment4.getWebsafeId(), Vote.Direction.UP, user);

    CollectionResponse<Comment> response =
        commentController.listComments(post.getWebsafeId(), Optional.absent(), Optional.absent(),
            user);

    assertEquals(4, response.getItems().size());

    for (Comment comment : response.getItems()) {
      assertEquals(2, comment.getShards().size());
      assertEquals(1, comment.getVoteCount());
      assertEquals(Vote.Direction.UP, comment.getDir());
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