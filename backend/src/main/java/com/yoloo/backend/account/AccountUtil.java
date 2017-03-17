package com.yoloo.backend.account;

import com.google.api.client.util.Strings;
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
    if (Strings.isNullOrEmpty(token.getName())) {
      final String email = token.getEmail();
      final int index = email.indexOf('@');
      return token.getEmail()
          .substring(0, index)
          .toLowerCase()
          .concat(String.valueOf(System.currentTimeMillis()).substring(5));
    }

    return token.getName()
        .trim()
        .replaceAll("\\s+", "")
        .substring(0, token.getName().length() > 10 ? 10 : token.getName().length())
        .toLowerCase()
        .concat(String.valueOf(System.currentTimeMillis()).substring(5));
  }
}
