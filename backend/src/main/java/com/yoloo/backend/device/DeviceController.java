package com.yoloo.backend.device;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class DeviceController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(DeviceController.class.getName());

  /**
   * Register device.
   *
   * @param regId the reg id
   * @param user the user
   */
  public void registerDevice(String regId, User user) {
    final Key<Account> accountKey = Key.create(user.getUserId());

    ofy().transact(() -> {
      try {
        DeviceRecord record = ofy().load().type(DeviceRecord.class)
            .ancestor(accountKey).first().safe();

        ofy().save().entity(record.withRegId(regId)).now();
      } catch (NotFoundException e) {
        DeviceRecord record = DeviceRecord.builder()
            .id(accountKey.toWebSafeString())
            .parent(accountKey)
            .regId(regId)
            .build();

        ofy().save().entity(record).now();
      }
    });
  }

  /**
   * Unregister device.
   *
   * @param regId the reg id
   * @param user the user
   */
  public void unregisterDevice(String regId, User user) {
    ofy().transact(() -> {
      Key<DeviceRecord> recordKey = ofy().load().type(DeviceRecord.class)
          .ancestor(Key.<Account>create(user.getUserId()))
          .keys().first().now();

      if (recordKey != null) {
        ofy().delete().key(recordKey).now();
      }
    });
  }
}
