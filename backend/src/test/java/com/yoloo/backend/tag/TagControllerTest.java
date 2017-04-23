package com.yoloo.backend.tag;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.common.base.Optional;
import com.yoloo.backend.util.TestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TagControllerTest extends TestBase {

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
  public void testSuggestTags_suggestByTagSimilarity() throws Exception {
    TagController tagController = TagControllerFactory.of().create();

    Tag cheap = tagController.insertTag("cheap");
    Tag lowBudget = tagController.insertTag("low budget");

    CollectionResponse<Tag> response =
        tagController.list(cheap.getName(), Optional.absent(), Optional.<Integer>absent());

    assertEquals(1, response.getItems().size());
  }
}
