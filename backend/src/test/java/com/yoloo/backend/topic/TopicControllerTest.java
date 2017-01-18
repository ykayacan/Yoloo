package com.yoloo.backend.topic;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Optional;
import com.yoloo.backend.topic.sort_strategy.CategorySorter;
import com.yoloo.backend.util.TestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TopicControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private TopicController topicController;

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

    topicController = TopicControllerFactory.of().create();
  }

  @Test
  public void testAddCategory_continent() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Topic topic = topicController.add("Europe", Topic.Type.CONTINENT, user);

    assertEquals("Europe", topic.getName());
    assertEquals(Topic.Type.CONTINENT, topic.getType());
    assertEquals(0, topic.getQuestions());
    assertEquals(0.0, topic.getRank(), 0);
    assertEquals(TopicCounterShard.SHARD_COUNT, topic.getShards().size());
  }

  @Test
  public void testAddCategory_theme() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Topic topic = topicController.add("Budget Travel", Topic.Type.THEME, user);

    assertEquals("Budget Travel", topic.getName());
    assertEquals(Topic.Type.THEME, topic.getType());
    assertEquals(0, topic.getQuestions());
    assertEquals(0.0, topic.getRank(), 0);
    assertEquals(TopicCounterShard.SHARD_COUNT, topic.getShards().size());
  }

  @Test
  public void testUpdateCategory_continent() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    Topic original = topicController.add("Budget Travel", Topic.Type.THEME, user);

    assertEquals("Budget Travel", original.getName());
    assertEquals(Topic.Type.THEME, original.getType());
    assertEquals(0, original.getQuestions());
    assertEquals(0.0, original.getRank(), 0);
    assertEquals(TopicCounterShard.SHARD_COUNT, original.getShards().size());

    Topic updated = topicController.update(original.getWebsafeId(),
        Optional.of("Female Travel"), Optional.<Topic.Type>absent(), user);

    assertEquals("Female Travel", updated.getName());
    assertEquals(Topic.Type.THEME, updated.getType());
    assertEquals(0, updated.getQuestions());
    assertEquals(0.0, updated.getRank(), 0);
    assertEquals(TopicCounterShard.SHARD_COUNT, updated.getShards().size());
  }

  @Test
  public void testListCategories_default() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    topicController.add("Budget Travel", Topic.Type.THEME, user);
    topicController.add("America", Topic.Type.CONTINENT, user);
    topicController.add("Female Travel", Topic.Type.THEME, user);

    CollectionResponse<Topic> response =
        topicController.list(Optional.<CategorySorter>absent(), Optional.<Integer>absent(),
            Optional.<String>absent(), user);

    assertEquals(3, response.getItems().size());
  }
}
