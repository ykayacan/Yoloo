package com.yoloo.backend.feed;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.AccountController;
import com.yoloo.backend.account.AccountControllerProvider;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.post.PostController;
import com.yoloo.backend.post.PostControllerFactory;
import com.yoloo.backend.util.TestBase;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FeedControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private PostController postController;

  @Override public void setUpGAE() {
    super.setUpGAE();

    helper.setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);
  }

  @Override public void setUp() {
    super.setUp();

    AccountController accountController = AccountControllerProvider.of().create();
    accountController.insertTestAccount();
    postController = PostControllerFactory.of().create();
  }

  @Test public void testFeedList() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    PostEntity postEntity = postController.insertQuestionPost("Test content", "", "", Optional.absent(),
        Optional.absent(), user);

    QueueFactory.getDefaultQueue().add(TaskOptions.Builder
        .withUrl("/tasks/update/feed")
        .param("accountId", user.getUserId())
        .param("postId", postEntity.getWebsafeId()));

    Thread.sleep(1000L);

    LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
    QueueStateInfo qsi =
        ltq.getQueueStateInfo().get(QueueFactory.getDefaultQueue().getQueueName());
    assertEquals(1, qsi.getTaskInfo().size());
    assertEquals("/tasks/update/feed", qsi.getTaskInfo().get(0).getUrl());

    Key<Feed> feed =
        ofy().load().type(Feed.class).keys().first().now();
    assertNotNull(feed);
  }
}
