package com.yoloo.backend.vote;

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
import com.yoloo.backend.account.AccountBundle;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentController;
import com.yoloo.backend.comment.CommentControllerFactory;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.group.TravelerGroupController;
import com.yoloo.backend.group.TravelerGroupControllerFactory;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.post.PostController;
import com.yoloo.backend.post.PostControllerFactory;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagController;
import com.yoloo.backend.tag.TagControllerFactory;
import com.yoloo.backend.util.TestBase;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

public class VoteControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private PostEntity postEntity;

  private CommentController commentController;
  private VoteController voteController;

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

    PostController postController = PostControllerFactory.of().create();
    commentController = CommentControllerFactory.of().create();
    TagController tagController = TagControllerFactory.of().create();
    TravelerGroupController travelerGroupController = TravelerGroupControllerFactory.of().create();
    voteController = VoteControllerFactory.of().create();

    AccountBundle model = createAccount();
    Account owner = model.getAccount();
    DeviceRecord record = createRecord(owner);
    Tracker tracker = GameService.create().createTracker(owner.getKey());

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    TravelerGroupEntity europe = travelerGroupController.insertGroup("europe", null);

    Tag passport = tagController.insertTag("passport");
    Tag visa = tagController.insertTag("visa");

    ImmutableList<Object> saveList = ImmutableList
        .builder()
        .add(owner)
        .addAll(model.getShards().values())
        .add(record)
        .add(tracker)
        .add(europe)
        .add(passport)
        .add(visa)
        .build();

    ofy().save().entities(saveList).now();

    postEntity =
        postController.insertQuestionPost("Test content", "visa,passport", europe.getWebsafeId(),
            Optional.absent(), Optional.absent(), user);
  }

  @Test
  public void testVoteComment_Up() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    final Key<Account> accountKey = Key.create(user.getUserId());

    Comment comment =
        commentController.insertComment(postEntity.getWebsafeId(), "Test comment", user);

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.UP.getValue(), user);

    CommentShardService shardService = CommentShardService.create();
    VoteService voteService = VoteService.create();

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.UP, comment.getDir());
    assertEquals(1L, comment.getVoteCount());
  }

  @Test
  public void testVoteComment_Down() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    final Key<Account> accountKey = Key.create(user.getUserId());

    Comment comment =
        commentController.insertComment(postEntity.getWebsafeId(), "Test comment", user);

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.DOWN.getValue(), user);

    CommentShardService shardService = CommentShardService.create();
    VoteService voteService = VoteService.create();

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.DOWN, comment.getDir());
    assertEquals(-1L, comment.getVoteCount());
  }

  @Test
  public void testVoteComment_Default() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    final Key<Account> accountKey = Key.create(user.getUserId());

    Comment comment =
        commentController.insertComment(postEntity.getWebsafeId(), "Test comment", user);

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.DEFAULT.getValue(), user);

    CommentShardService shardService = CommentShardService.create();
    VoteService voteService = VoteService.create();

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0L, comment.getVoteCount());
  }

  @Test
  public void testVoteComment_UpToDefault() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    final Key<Account> accountKey = Key.create(user.getUserId());

    CommentShardService shardService = CommentShardService.create();
    VoteService voteService = VoteService.create();

    Comment comment =
        commentController.insertComment(postEntity.getWebsafeId(), "Test comment", user);

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.UP.getValue(), user);

    comment = shardService
        .mergeShards(comment)
        .flatMap(c -> voteService.checkCommentVote(c, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.UP, comment.getDir());
    assertEquals(1L, comment.getVoteCount());

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.DEFAULT.getValue(), user);

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0L, comment.getVoteCount());
  }

  @Test
  public void testVoteComment_DefaultToUp() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    final Key<Account> accountKey = Key.create(user.getUserId());

    Comment comment =
        commentController.insertComment(postEntity.getWebsafeId(), "Test comment", user);

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.DEFAULT.getValue(), user);

    CommentShardService shardService = CommentShardService.create();
    VoteService voteService = VoteService.create();

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0L, comment.getVoteCount());

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.UP.getValue(), user);

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.UP, comment.getDir());
    assertEquals(1L, comment.getVoteCount());
  }

  @Test
  public void testVoteComment_DefaultToDown() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    final Key<Account> accountKey = Key.create(user.getUserId());

    Comment comment =
        commentController.insertComment(postEntity.getWebsafeId(), "Test comment", user);

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.DEFAULT.getValue(), user);

    CommentShardService shardService = CommentShardService.create();
    VoteService voteService = VoteService.create();

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0L, comment.getVoteCount());

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.DOWN.getValue(), user);

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.DOWN, comment.getDir());
    assertEquals(-1L, comment.getVoteCount());
  }

  @Test
  public void testVoteComment_DownToDefault() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();
    final Key<Account> accountKey = Key.create(user.getUserId());

    Comment comment =
        commentController.insertComment(postEntity.getWebsafeId(), "Test comment", user);

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.DOWN.getValue(), user);

    CommentShardService shardService = CommentShardService.create();
    VoteService voteService = VoteService.create();

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.DOWN, comment.getDir());
    assertEquals(-1L, comment.getVoteCount());

    voteController.voteComment(comment.getWebsafeId(), Vote.Direction.DEFAULT.getValue(), user);

    comment = shardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
        .blockingSingle();

    assertEquals(Vote.Direction.DEFAULT, comment.getDir());
    assertEquals(0L, comment.getVoteCount());
  }

  private AccountBundle createAccount() {
    final Key<Account> ownerKey = fact().allocateId(Account.class);

    AccountShardService ass = AccountShardService.create();

    Map<Ref<AccountShard>, AccountShard> map = ass.createShardMapWithRef(ownerKey);

    Account account = Account
        .builder()
        .id(ownerKey.getId())
        .avatarUrl(new Link("Test avatar"))
        .email(new Email(USER_EMAIL))
        .username("Test user")
        .shardRefs(Lists.newArrayList(map.keySet()))
        .created(DateTime.now())
        .build();

    return AccountBundle.builder().account(account).shards(map).build();
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