package com.yoloo.backend.group;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.yoloo.backend.travelertype.TravelerTypeEndpoint;
import com.yoloo.backend.travelertype.TravelerTypeEntity;
import com.yoloo.backend.util.TestBase;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TravelerGroupServiceTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  private static final String TEST_IMG = "dev_test";

  private TravelerGroupEndpoint groupEndpoint;
  private TravelerTypeEndpoint typeEndpoint;

  @Override
  public void setUpGAE() {
    super.setUpGAE();

    helper
        .setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);

    groupEndpoint = new TravelerGroupEndpoint();
    typeEndpoint = new TravelerTypeEndpoint();
  }

  @Test
  public void testFindGroupKeys() throws Exception {
    User user = UserServiceFactory.getUserService().getCurrentUser();

    TravelerGroupEntity group1 = groupEndpoint.insert("Adventure", TEST_IMG, user);
    TravelerGroupEntity group2 = groupEndpoint.insert("Activities", TEST_IMG, user);
    TravelerGroupEntity group3 = groupEndpoint.insert("Solo Travel", TEST_IMG, user);

    TravelerTypeEntity type1 =
        typeEndpoint.insert("Thrill-Seeker", TEST_IMG, combineIds(group1, group2), user);
    TravelerTypeEntity type2 =
        typeEndpoint.insert("Solo Travel", TEST_IMG, combineIds(group2, group3), user);

    List<String> travelerTypeIds = new ArrayList<>();
    travelerTypeIds.add(type1.getWebsafeId());
    travelerTypeIds.add(type2.getWebsafeId());

    TravelerGroupService service = new TravelerGroupService();
    List<Key<TravelerGroupEntity>> keys = service.findGroupKeys(travelerTypeIds);

    assertEquals(3, keys.size());
  }

  private String combineIds(TravelerGroupEntity... entities) {
    return Stream
        .of(entities)
        .map(TravelerGroupEntity::getWebsafeId)
        .collect(Collectors.joining(","));
  }
}
