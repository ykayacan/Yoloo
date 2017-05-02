package com.yoloo.backend.authentication.authenticators;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.Singleton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;

import static com.yoloo.backend.OfyService.ofy;

@Singleton
public class FirebaseAuthenticator implements Authenticator {

  @Override
  public User authenticate(final HttpServletRequest request) {
    String authHeader = request.getHeader(OAuth2.HeaderType.AUTHORIZATION);

    if (Strings.isNullOrEmpty(authHeader) || !authHeader.contains(OAuth2.OAUTH_HEADER_NAME)) {
      return null;
    }

    // [0] - Bearer
    // [1] - Token
    final String idToken = authHeader.split(" ")[1];

    Task<FirebaseToken> authTask =
        FirebaseAuth.getInstance().verifyIdToken(idToken).addOnSuccessListener(decodedToken -> {
        });

    if (isTaskCompleted(authTask) && authTask.isSuccessful()) {
      FirebaseToken token = authTask.getResult();

      final Key<Account> accountKey = ofy().load()
          .type(Account.class)
          .filter(Account.FIELD_EMAIL + " =", token.getEmail())
          .keys()
          .first()
          .now();

      if (accountKey != null) {
        return new User(accountKey.toWebSafeString(), token.getEmail());
      }

      return null;
    }

    return null;
  }

  private boolean isTaskCompleted(Task<FirebaseToken> authTask) {
    try {
      Tasks.await(authTask);
    } catch (InterruptedException | ExecutionException e) {
      return false;
    }
    return true;
  }
}