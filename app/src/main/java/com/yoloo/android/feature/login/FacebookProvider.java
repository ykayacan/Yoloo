/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yoloo.android.feature.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.bluelinelabs.conductor.Controller;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.yoloo.android.R;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import timber.log.Timber;

public class FacebookProvider implements IdpProvider, FacebookCallback<LoginResult> {
  private static final String EMAIL = "email";
  private static final String PUBLIC_PROFILE = "public_profile";
  private static final String ERROR = "err";
  private static final String ERROR_MSG = "err_msg";

  private static CallbackManager callbackManager;

  private final List<String> scopes;
  // DO NOT USE DIRECTLY: see onSuccess(String, LoginResult) and onFailure(Bundle) below
  private IdpCallback callbackObject;

  public FacebookProvider(AuthUI.IdpConfig idpConfig) {
    List<String> scopes = idpConfig.getScopes();
    if (scopes == null) {
      this.scopes = new ArrayList<>();
    } else {
      this.scopes = scopes;
    }
  }

  @Nullable public static AuthCredential createAuthCredential(IdpResponse response) {
    if (!response.getProviderType().equals(FacebookAuthProvider.PROVIDER_ID)) {
      return null;
    }

    return FacebookAuthProvider.getCredential(response.getIdpToken());
  }

  @Override public String getName(Context context) {
    return context.getResources().getString(R.string.idp_name_facebook);
  }

  @Override public String getProviderId() {
    return FacebookAuthProvider.PROVIDER_ID;
  }

  @Override public void startLogin(Controller controller) {
    controller.registerForActivityResult(
        CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode());

    callbackManager = CallbackManager.Factory.create();
    LoginManager loginManager = LoginManager.getInstance();
    loginManager.registerCallback(callbackManager, this);

    List<String> permissionsList = new ArrayList<>(scopes);

    // Ensure we have email and public_profile scopes
    permissionsList.add(EMAIL);
    permissionsList.add(PUBLIC_PROFILE);

    loginManager.logInWithReadPermissions(controller.getActivity(), permissionsList);
  }

  @Override public void setAuthenticationCallback(IdpCallback callback) {
    callbackObject = callback;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (callbackManager != null) {
      callbackManager.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override public void onSuccess(final LoginResult loginResult) {
    Timber.d("onSuccess(): %s", loginResult.getAccessToken());
    GraphRequest request = GraphRequest.newMeRequest(
        loginResult.getAccessToken(),
        (object, response) -> {
          FacebookRequestError requestError = response.getError();
          if (requestError != null) {
            Timber.e("Received Facebook error: %s", requestError.getErrorMessage());
            onFailure(new Bundle());
            return;
          }
          if (object == null) {
            Timber.w("Received null response from Facebook GraphRequest");
            onFailure(new Bundle());
          } else {
            try {
              String email = object.getString("email");
              onSuccess(email, loginResult);
            } catch (JSONException e) {
              Timber.e(e, "JSON Exception reading from Facebook GraphRequest");
              onFailure(new Bundle());
            }
          }
        });

    Bundle parameters = new Bundle();
    parameters.putString("fields", "id,name,email,gender,birthday");
    request.setParameters(parameters);
    request.executeAsync();
  }

  @Override public void onCancel() {
    Bundle extra = new Bundle();
    extra.putString(ERROR, "cancelled");
    onFailure(extra);
  }

  @Override public void onError(FacebookException error) {
    Timber.e("Error logging in with Facebook. %s", error.getMessage());
    Bundle extra = new Bundle();
    extra.putString(ERROR, "error");
    extra.putString(ERROR_MSG, error.getMessage());
    onFailure(extra);
  }

  private IdpResponse createIdpResponse(String email, LoginResult loginResult) {
    return new IdpResponse(FacebookAuthProvider.PROVIDER_ID, email,
        loginResult.getAccessToken().getToken());
  }

  private void onSuccess(String email, LoginResult loginResult) {
    gcCallbackManager();
    callbackObject.onSuccess(createIdpResponse(email, loginResult));
  }

  private void onFailure(Bundle bundle) {
    gcCallbackManager();
    callbackObject.onFailure(bundle);
  }

  private void gcCallbackManager() {
    // callbackManager must be static to prevent it from being destroyed if the activity
    // containing FacebookProvider dies.
    // In startLogin(Activity), LoginManager#registerCallback(CallbackManager, FacebookCallback)
    // stores the FacebookCallback parameter--in this case a FacebookProvider instance--into
    // a HashMap in the CallbackManager instance, callbackManager.
    // Because FacebookProvider which contains an instance of an activity, callbackObject,
    // is contained in callbackManager, that activity will not be garbage collected.
    // Thus, we have leaked an Activity.
    callbackManager = null;
  }
}
