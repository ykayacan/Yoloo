package com.yoloo.backend.bookmark;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountBundle;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

public class BookmarkControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private PostEntity postEntity;

  private PostController postController;
  private BookmarkController bookmarkController;
  private TagController tagController;
  private TravelerGroupController travelerGroupController;

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
    bookmarkController = BookmarkControllerFactory.of().create();
    tagController = TagControllerFactory.of().create();
    travelerGroupController = TravelerGroupControllerFactory.of().create();

    AccountBundle model = createAccount();

    Account owner = model.getAccount();
    DeviceRecord record = createRecord(owner);
    Tracker tracker = GameService.create().createTracker(owner.getKey());

    User user = new User(USER_EMAIL, USER_AUTH_DOMAIN, owner.getWebsafeId());

    TravelerGroupEntity europe = travelerGroupController.insertGroup("europe", "dev");

    Tag passport = tagController.insertTag("passport");
    Tag visa = tagController.insertTag("visa");

    ImmutableSet<Object> saveList = ImmutableSet
        .builder()
        .add(owner)
        .addAll(model.getShards().values())
        .add(tracker)
        .add(europe)
        .add(passport)
        .add(visa)
        .add(record)
        .build();

    ofy().save().entities(saveList).now();

    postEntity =
        postController.insertQuestionPost("Test content", "visa,passport", europe.getWebsafeId(),
            Optional.absent(), Optional.absent(), user);
  }

  @Test
  public void testBookmark() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    bookmarkController.insertBookmark(postEntity.getWebsafeId(), user);

    List<Bookmark> bookmarks =
        ofy().load().type(Bookmark.class).ancestor(Key.<Account>create(user.getUserId())).list();

    assertEquals(1, bookmarks.size());
  }

  @Test
  public void testUnbookmark() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    bookmarkController.insertBookmark(postEntity.getWebsafeId(), user);

    List<Bookmark> bookmarks1 =
        ofy().load().type(Bookmark.class).ancestor(Key.<Account>create(user.getUserId())).list();

    assertEquals(1, bookmarks1.size());

    bookmarkController.deleteBookmark(postEntity.getWebsafeId(), user);

    List<Bookmark> bookmarks2 =
        ofy().load().type(Bookmark.class).ancestor(Key.<Account>create(user.getUserId())).list();

    assertEquals(0, bookmarks2.size());
  }

  @Test
  public void testListSavedQuestions() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    PostEntity postEntity2 =
        postController.insertQuestionPost("Test content", "visa,passport", "europe",
            Optional.absent(), Optional.absent(), user);

    bookmarkController.insertBookmark(postEntity.getWebsafeId(), user);
    bookmarkController.insertBookmark(postEntity2.getWebsafeId(), user);

    List<Bookmark> bookmarks =
        ofy().load().type(Bookmark.class).ancestor(Key.<Account>create(user.getUserId())).list();

    assertEquals(2, bookmarks.size());
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
