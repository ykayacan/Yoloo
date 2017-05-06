package com.yoloo.backend.device;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import io.reactivex.Observable;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceUtil {

  public static List<Key<DeviceRecord>> createKeysFromAccount(List<Key<Account>> accountKeys) {
    return Observable.fromIterable(accountKeys).map(DeviceRecord::createKey).toList().blockingGet();
  }
}
