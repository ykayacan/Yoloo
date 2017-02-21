package com.yoloo.backend.account;

import com.google.firebase.auth.FirebaseToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountUtil {

  public static boolean isUserRegistered(FirebaseToken token) {
    return ofy().load().type(Account.class)
        .filter(Account.FIELD_EMAIL + " =", token.getEmail())
        .keys().first().now() != null;
  }
}
