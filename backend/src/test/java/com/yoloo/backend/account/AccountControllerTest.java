package com.yoloo.backend.account;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.yoloo.backend.relationship.Relationship;
import com.yoloo.backend.util.TestBase;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

public class AccountControllerTest extends TestBase {

  @Test public void returnThreeNewUsersForGivenUser() throws ServiceException {
    Account me = Account.builder().id(1).created(DateTime.now()).build();
    Account user1 = Account.builder().id(2).created(DateTime.now().plusHours(1)).build();
    Account user2 = Account.builder().id(3).created(DateTime.now().plusMinutes(5)).build();
    Account user3 = Account.builder().id(4).created(DateTime.now().plusHours(8)).build();

    ofy().save().entities(me, user1, user2, user3).now();

    AccountController controller = AccountControllerProvider.of().create();

    CollectionResponse<Account> users = controller.listNewUsers(
        Optional.absent(), Optional.absent(), new User("", "", me.getWebsafeId()));

    assertEquals(3, users.getItems().size());
  }

  @Test public void returnNewUsersAfterFollowedForGivenUser() throws ServiceException {
    Account me = Account.builder().id(1).created(DateTime.now()).build();
    Account user1 = Account.builder().id(2).created(DateTime.now().plusHours(1)).build();
    Account user2 = Account.builder().id(3).created(DateTime.now().plusMinutes(5)).build();
    Account user3 = Account.builder().id(4).created(DateTime.now().plusHours(8)).build();

    Relationship relationship =
        Relationship.builder().followingKey(user1.getKey()).followerKey(me.getKey()).build();

    ofy().save().entities(me, user1, user2, user3, relationship).now();

    AccountController controller = AccountControllerProvider.of().create();

    CollectionResponse<Account> users = controller.listNewUsers(
        Optional.absent(), Optional.absent(), new User("", "", me.getWebsafeId()));

    assertEquals(2, users.getItems().size());
  }
}
