package com.yoloo.backend.post;

import com.google.api.server.spi.ServiceException;
import com.google.appengine.api.users.User;
import com.yoloo.backend.Constant;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.UserEndpoint;
import com.yoloo.backend.device.DeviceEndpoint;
import com.yoloo.backend.group.TravelerGroupEndpoint;
import com.yoloo.backend.tag.TagEndpoint;
import com.yoloo.backend.util.TestBase;
import org.junit.Test;

public class PostEndpointTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private User user1;
  private User user2;

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

    TravelerGroupEndpoint travelerGroupEndpoint = new TravelerGroupEndpoint();
    try {
      travelerGroupEndpoint.setup("dev");
    } catch (ServiceException e) {
      e.printStackTrace();
    }

    UserEndpoint userEndpoint = new UserEndpoint();
    DeviceEndpoint deviceEndpoint = new DeviceEndpoint();
    try {
      Account account = userEndpoint.registerUserTest(Constant.USER_1);
      user1 = new User("", "", account.getWebsafeId());
      deviceEndpoint.registerDevice("test1", user1);
    } catch (ServiceException e) {
      e.printStackTrace();
    }

    try {
      Account account = userEndpoint.registerUserTest(Constant.USER_2);
      user2 = new User("", "", account.getWebsafeId());
      deviceEndpoint.registerDevice("test2", user2);
    } catch (ServiceException e) {
      e.printStackTrace();
    }

    TagEndpoint tagEndpoint = new TagEndpoint();
    try {
      tagEndpoint.insert("tag1", user1);
    } catch (ServiceException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testCreateFeed() throws Exception {
    /*QuestionEndpoint questionEndpoint = new QuestionEndpoint();
    questionEndpoint.insert("Hello", "test1", )*/
  }
}
