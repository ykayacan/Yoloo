package com.yoloo.backend.device;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Value
@Builder
public class DeviceRecord {

  public static final String FIELD_REGID = "regId";

  /**
   * Websafe {@code Account} ID with parentUserKey.
   */
  @Id
  private String id;

  @Parent
  private Key<Account> parentUserKey;

  @Index
  @NonFinal
  @Wither
  private String regId;

  public static Key<DeviceRecord> createKey(Key<Account> parentUserKey) {
    return Key.create(parentUserKey, DeviceRecord.class, parentUserKey.toWebSafeString());
  }
}