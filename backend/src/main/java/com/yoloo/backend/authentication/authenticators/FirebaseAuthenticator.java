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

    if (Strings.isNullOrEmpty(authHeader) ||
        !authHeader.contains(OAuth2.OAUTH_HEADER_NAME)) {
      return null;
    }

    final String idToken = authHeader.split(" ")[1];

    /*return Single
        .create((SingleOnSubscribe<FirebaseToken>) e -> {
          Task<FirebaseToken> authTask = FirebaseAuth.getInstance().verifyIdToken(idToken);

          authTask.addOnSuccessListener(e::onSuccess);
          authTask.addOnFailureListener(e::onError);
        })
        .map(token -> {
          Key<Account> accountKey = ofy().load().type(Account.class)
              .filter(Account.FIELD_FIREBASE_UUID + " =", token.getUid()).keys().first().now();

          return new User(accountKey.toWebSafeString(), token.getEmail());
        })
        .blockingGet();*/

    Task<FirebaseToken> authTask = FirebaseAuth.getInstance().verifyIdToken(idToken)
        .addOnSuccessListener(decodedToken -> {
        });

    try {
      Tasks.await(authTask);
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return null;
    }

    FirebaseToken token = authTask.getResult();

    Key<Account> accountKey = ofy().load().type(Account.class)
        .filter(Account.FIELD_FIREBASE_UUID + " =", token.getUid()).keys().first().now();

    return new User(accountKey.toWebSafeString(), token.getEmail());
  }
}