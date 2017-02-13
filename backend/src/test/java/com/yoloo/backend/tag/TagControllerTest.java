package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
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

  @Override
  public void setUpGAE() {
    super.setUpGAE();

    helper.setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);
  }

  @Test
  public void testAddGroup() throws Exception {
    TagController tagController = TagControllerFactory.of().create();

    Tag group = tagController.insertGroup("test group");

    assertEquals("test group", group.getName());
    assertEquals(0, group.getPosts());
    assertEquals(0, group.getTotalTagCount());
  }

  @Test
  public void testUpdateGroup() throws Exception {
    TagController tagController = TagControllerFactory.of().create();

    Tag original = tagController.insertGroup("test group");

    Tag updated = tagController
        .updateGroup(original.getWebsafeId(), Optional.of("test group 2"));

    assertEquals(original.getKey(), updated.getKey());
    assertEquals(original.getTotalTagCount(), updated.getTotalTagCount());
    assertEquals(original.getPosts(), updated.getPosts());
    assertEquals("test group 2", updated.getName());
  }

  @Test(expected = NotFoundException.class)
  public void testDeleteGroup() throws Exception {
    TagController tagController = TagControllerFactory.of().create();

    Tag original = tagController.insertGroup("test group");

    ofy().save().entity(original).now();

    tagController.deleteGroup(original.getWebsafeId());

    ofy().load().key(original.getKey()).safe();
  }

  @Test
  public void testAddTag_singleGroup() throws Exception {
    TagController tagController = TagControllerFactory.of().create();

    Tag group = tagController.insertGroup("test group");

    Tag tag = tagController
        .insertTag("tag1", Locale.ENGLISH.getISO3Language(), group.getWebsafeId());

    assertEquals("tag1", tag.getName());
    assertEquals(Locale.ENGLISH.getISO3Language(), tag.getLanguage());
    assertEquals(TagShard.SHARD_COUNT, tag.getShards().size());

    List<Key<Tag>> keys = new ArrayList<>(1);
    keys.add(group.getKey());

    assertEquals(keys, tag.getGroupKeys());
    assertEquals(0, tag.getPosts());
  }

  @Test
  public void testAddTag_multipleGroup() throws Exception {
    TagController tagController = TagControllerFactory.of().create();

    Tag group1 = tagController.insertGroup("test group");
    Tag group2 = tagController.insertGroup("test group 2");

    String groupIds = group1.getWebsafeId() + "," + group2.getWebsafeId();

    Tag tag = tagController.insertTag("h1", Locale.ENGLISH.getISO3Language(), groupIds);

    assertEquals("h1", tag.getName());
    assertEquals(Locale.ENGLISH.getISO3Language(), tag.getLanguage());

    List<Key<Tag>> keys = new ArrayList<>(1);
    keys.add(group1.getKey());
    keys.add(group2.getKey());

    assertEquals(keys, tag.getGroupKeys());
    assertEquals(0, tag.getPosts());
  }

  @Test
  public void testSuggestTags_suggestByTagSimilarity() throws Exception {
    TagController tagController = TagControllerFactory.of().create();

    Tag group1 = tagController.insertGroup("travel");
    Tag group2 = tagController.insertGroup("budget");

    Tag cheap = tagController.insertTag("cheap", "en", group2.getWebsafeId());
    Tag lowBudget = tagController.insertTag("low budget", "en", group2.getWebsafeId());

    CollectionResponse<Tag> response =
        tagController.list(cheap.getName(), Optional.absent(), Optional.<Integer>absent());

    assertEquals(1, response.getItems().size());
  }

  @Test
  public void testSuggestTags_suggestByGroup() throws Exception {
    TagController tagController = TagControllerFactory.of().create();

    Tag group1 = tagController.insertGroup("travel");
    Tag group2 = tagController.insertGroup("budget");

    tagController.insertTag("female travel", "en", group1.getWebsafeId());
    tagController.insertTag("camp", "en", group1.getWebsafeId());

    tagController.insertTag("cheap", "en", group2.getWebsafeId());
    tagController.insertTag("low budget", "en", group2.getWebsafeId());

    CollectionResponse<Tag> response =
        tagController.list(group1.getName(), Optional.absent(), Optional.<Integer>absent());

    assertEquals(2, response.getItems().size());
  }
}
