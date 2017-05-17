package com.yoloo.backend.device;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class DeviceRecord {

  public static final String FIELD_REG_ID = "regId";

  /**
   * Websafe {@code Account} ID with parent.
   */
  @Id
  private String id;

  @Parent
  @NonFinal
  private Key<Account> parent;

  @NonFinal
  @Wither
  private String regId;

  public static Key<DeviceRecord> createKey(Key<Account> parentUserKey) {
    return Key.create(parentUserKey, DeviceRecord.class, parentUserKey.toWebSafeString());
  }
}