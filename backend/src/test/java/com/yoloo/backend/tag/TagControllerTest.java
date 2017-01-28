package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.backend.util.TestBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

public class TagControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private TagController tagController;

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

    tagController = TagControllerFactory.of().create();
  }

  @Test
  public void testAddGroup() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Tag group = tagController.addGroup("test group", user);

    assertEquals("test group", group.getName());
    assertEquals(0, group.getQuestions());
    assertEquals(0, group.getTotalTagCount());
  }

  @Test
  public void testUpdateGroup() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Tag original = tagController.addGroup("test group", user);

    Tag updated = tagController
        .updateGroup(original.getWebsafeId(), Optional.of("test group 2"), user);

    assertEquals(original.getKey(), updated.getKey());
    assertEquals(original.getTotalTagCount(), updated.getTotalTagCount());
    assertEquals(original.getQuestions(), updated.getQuestions());
    assertEquals("test group 2", updated.getName());
  }

  @Test(expected = NotFoundException.class)
  public void testDeleteGroup() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Tag original = tagController.addGroup("test group", user);

    ofy().save().entity(original).now();

    tagController.deleteGroup(original.getWebsafeId(), user);

    ofy().load().key(original.getKey()).safe();
  }

  @Test
  public void testAddTag_singleGroup() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Tag group = tagController.addGroup("test group", user);

    Tag tag = tagController
        .addTag("tag1", Locale.ENGLISH.getISO3Language(), group.getWebsafeId(), user);

    assertEquals("tag1", tag.getName());
    assertEquals(Locale.ENGLISH.getISO3Language(), tag.getLanguage());
    assertEquals(TagCounterShard.SHARD_COUNT, tag.getShards().size());

    List<Key<Tag>> keys = new ArrayList<>(1);
    keys.add(group.getKey());

    assertEquals(keys, tag.getGroupKeys());
    assertEquals(0, tag.getQuestions());
  }

  @Test
  public void testAddTag_multipleGroup() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Tag group1 = tagController.addGroup("test group", user);
    Tag group2 = tagController.addGroup("test group 2", user);

    String groupIds = group1.getWebsafeId() + "," + group2.getWebsafeId();

    Tag tag = tagController.addTag("h1", Locale.ENGLISH.getISO3Language(), groupIds, user);

    assertEquals("h1", tag.getName());
    assertEquals(Locale.ENGLISH.getISO3Language(), tag.getLanguage());

    List<Key<Tag>> keys = new ArrayList<>(1);
    keys.add(group1.getKey());
    keys.add(group2.getKey());

    assertEquals(keys, tag.getGroupKeys());
    assertEquals(0, tag.getQuestions());
  }

  @Test
  public void testSuggestTags_suggestByTagSimilarity() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Tag group1 = tagController.addGroup("travel", user);
    Tag group2 = tagController.addGroup("budget", user);

    Tag cheap = tagController.addTag("cheap", "en", group2.getWebsafeId(), user);
    Tag lowBudget = tagController.addTag("low budget", "en", group2.getWebsafeId(), user);

    CollectionResponse<Tag> response =
        tagController.list(cheap.getName(), Optional.absent(), Optional.<Integer>absent(), user);

    assertEquals(1, response.getItems().size());
  }

  @Test
  public void testSuggestTags_suggestByGroup() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Tag group1 = tagController.addGroup("travel", user);
    Tag group2 = tagController.addGroup("budget", user);

    tagController.addTag("female travel", "en", group1.getWebsafeId(), user);
    tagController.addTag("camp", "en", group1.getWebsafeId(), user);

    tagController.addTag("cheap", "en", group2.getWebsafeId(), user);
    tagController.addTag("low budget", "en", group2.getWebsafeId(), user);

    CollectionResponse<Tag> response =
        tagController.list(group1.getName(), Optional.absent(), Optional.<Integer>absent(), user);

    assertEquals(2, response.getItems().size());
  }
}
