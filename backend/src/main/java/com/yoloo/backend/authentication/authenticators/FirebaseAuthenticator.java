package com.yoloo.backend.authentication.authenticators;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.Singleton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import com.yoloo.backend.authentication.oauth2.OAuth2;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

@Singleton
public class FirebaseAuthenticator implements Authenticator {

    @Override
    public User authenticate(final HttpServletRequest request) {
        String authHeader = request.getHeader(OAuth2.HeaderType.AUTHORIZATION);

        if (Strings.isNullOrEmpty(authHeader) ||
                !authHeader.contains(OAuth2.OAUTH_HEADER_NAME)) {
            return null;
        }

        String idToken = authHeader.split(" ")[1];

        Task<FirebaseToken> authTask = FirebaseAuth.getInstance().verifyIdToken(idToken)
                .addOnSuccessListener(new OnSuccessListener<FirebaseToken>() {
                    @Override
                    public void onSuccess(FirebaseToken decodedToken) {
                    }
                });

        try {
            Tasks.await(authTask);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        FirebaseToken token = authTask.getResult();

        return new User(token.getUid(), token.getEmail());
    }
}
