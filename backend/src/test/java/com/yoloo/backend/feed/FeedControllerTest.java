package com.yoloo.backend.feed;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.AccountController;
import com.yoloo.backend.account.AccountControllerProvider;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.category.CategoryController;
import com.yoloo.backend.category.CategoryControllerFactory;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.post.PostController;
import com.yoloo.backend.post.PostControllerFactory;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagController;
import com.yoloo.backend.tag.TagControllerFactory;
import com.yoloo.backend.util.TestBase;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.ofy;

public class FeedControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private Category budgetTravel;
  private Category europe;

  private Tag passport;

  private Tag visa;
  private Tag visa2;

  private PostController postController;

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

    AccountController accountController = AccountControllerProvider.of().create();
    postController = PostControllerFactory.of().create();
    TagController tagController = TagControllerFactory.of().create();
    CategoryController categoryController = CategoryControllerFactory.of().create();

    try {
      accountController.insertAdmin();
    } catch (ConflictException e) {
      e.printStackTrace();
    }

    try {
      budgetTravel = categoryController.insertCategory("budget travel", Category.Type.THEME);
    } catch (ConflictException e) {
      e.printStackTrace();
    }
    try {
      europe = categoryController.insertCategory("europe", Category.Type.THEME);
    } catch (ConflictException e) {
      e.printStackTrace();
    }

    passport = tagController.insertGroup("passport");

    visa = tagController.insertTag("visa", "en", passport.getWebsafeId());
    visa2 = tagController.insertTag("visa2", "en", passport.getWebsafeId());
  }

  @Test
  public void testFeedList() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    String tags = visa.getName() + "," + visa2.getName();
    String categories = europe.getName() + "," + budgetTravel.getName();

    Post post = postController.insertQuestion("Test content", tags, categories, Optional.absent(),
        Optional.absent(), user);

    Feed feed = Feed.builder()
        .parent(post.getParent())
        .postRef(Ref.create(post))
        .build();

    ofy().save().entity(feed).now();
  }
}
