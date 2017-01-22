package com.yoloo.backend.device;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.util.TestBase;
import java.util.UUID;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeviceControllerTest extends TestBase {

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
  public void testRegisterDevice() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    DeviceController controller = getDeviceController();

    String regId = UUID.randomUUID().toString();

    controller.registerDevice(regId, user);

    DeviceRecord record = ofy().load().type(DeviceRecord.class)
        .filter(DeviceRecord.FIELD_REG_ID + " =", regId)
        .ancestor(Key.<Account>create(user.getUserId()))
        .first().now();

    assertNotNull(record);
    assertEquals(regId, record.getRegId());
    assertEquals(user.getUserId(), record.getParentUserKey().toWebSafeString());
  }

  @Test(expected = NotFoundException.class)
  public void testUnregisterDevice() throws Exception {
    final User user = UserServiceFactory.getUserService().getCurrentUser();

    DeviceController controller = getDeviceController();

    String regId = UUID.randomUUID().toString();

    controller.registerDevice(regId, user);

    DeviceRecord record = ofy().load().type(DeviceRecord.class)
        .filter(DeviceRecord.FIELD_REG_ID + " =", regId)
        .ancestor(Key.<Account>create(user.getUserId()))
        .first().now();

    assertNotNull(record);
    assertEquals(regId, record.getRegId());
    assertEquals(user.getUserId(), record.getParentUserKey().toWebSafeString());

    controller.unregisterDevice(regId, user);

    ofy().load().type(DeviceRecord.class)
        .filter(DeviceRecord.FIELD_REG_ID + " =", regId)
        .ancestor(Key.<Account>create(user.getUserId()))
        .first().safe();
  }

  private DeviceController getDeviceController() {
    return DeviceController.create();
  }
}
