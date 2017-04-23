package com.yoloo.backend.tag;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.yoloo.backend.util.TestBase;
import ix.Ix;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TagServiceTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  @Override
  public void setUpGAE() {
    super.setUpGAE();

    helper
        .setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);
  }

  @Test
  public void testUpdateTagsWithPersistentTags() throws Exception {
    User user = UserServiceFactory.getUserService().getCurrentUser();

    TagEndpoint tagEndpoint = new TagEndpoint();
    Tag tag1 = tagEndpoint.insert("test1", user);
    Tag tag2 = tagEndpoint.insert("test2", user);

    TagService tagService = new TagService();
    List<Tag> updated = tagService.updateTags(Arrays.asList(tag1.getName(), tag2.getName()));

    Ix.from(updated).foreach(tag -> assertEquals(1, tag.getPostCount()));
  }

  @Test
  public void testUpdateTagsWithNewTags() throws Exception {
    TagService tagService = new TagService();
    List<Tag> updated =
        tagService.updateTags(Arrays.asList("test1", "test2", "test3"));

    Ix.from(updated).foreach(tag -> assertEquals(1, tag.getPostCount()));
  }

  @Test
  public void testUpdateTagsWithNewAndPersistentTags() throws Exception {
    User user = UserServiceFactory.getUserService().getCurrentUser();

    TagEndpoint tagEndpoint = new TagEndpoint();
    Tag tag1 = tagEndpoint.insert("test1", user);
    Tag tag2 = tagEndpoint.insert("test2", user);

    TagService tagService = new TagService();
    List<Tag> updated =
        tagService.updateTags(Arrays.asList("test1", "test2", "test3"));

    Ix.from(updated).foreach(tag -> assertEquals(1, tag.getPostCount()));
  }
}
