package com.yoloo.backend.account;

import com.google.firebase.auth.FirebaseToken;
import lombok.experimental.UtilityClass;

import static com.yoloo.backend.OfyService.ofy;

@UtilityClass
public final class AccountUtil {

  public static boolean isUserRegistered(FirebaseToken token) {
    return ofy().load().type(Account.class)
        .filter(Account.FIELD_EMAIL + " =", token.getEmail())
        .keys().first().now() != null;
  }

  public static String generateUsername(FirebaseToken token) {
    return token.getName()
        .trim()
        .replaceAll("\\s+", "")
        .toLowerCase()
        .substring(0, 10)
        .concat(String.valueOf(System.currentTimeMillis()).substring(5));
  }
}
