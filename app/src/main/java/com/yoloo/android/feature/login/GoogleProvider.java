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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.bluelinelabs.conductor.Controller;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.yoloo.android.R;
import com.yoloo.android.feature.login.AuthUI.IdpConfig;

public class GoogleProvider implements IdpProvider, GoogleApiClient.OnConnectionFailedListener {
  private static final String TAG = "GoogleProvider";
  private static final int RC_SIGN_IN = 20;
  private static final String ERROR_KEY = "error";

  private GoogleApiClient googleApiClient;
  private Controller controller;
  private IdpConfig idpConfig;
  private IdpCallback idpCallback;

  public GoogleProvider(Controller controller, IdpConfig idpConfig) {
    this(controller, idpConfig, null);
  }

  public GoogleProvider(Controller controller, IdpConfig idpConfig, @Nullable String email) {
    this.controller = controller;
    this.idpConfig = idpConfig;

    googleApiClient = new GoogleApiClient.Builder(controller.getActivity())
        .addApi(Auth.GOOGLE_SIGN_IN_API, getSignInOptions(email))
        .build();
  }

  @Nullable public static AuthCredential createAuthCredential(IdpResponse response) {
    return GoogleAuthProvider.getCredential(response.getIdpToken(), null);
  }

  private GoogleSignInOptions getSignInOptions(@Nullable String email) {
    final String clientId = controller.getActivity().getString(R.string.default_web_client_id);

    GoogleSignInOptions.Builder builder =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(clientId);

    // Add additional scopes
    for (String scopeString : idpConfig.getScopes()) {
      builder.requestScopes(new Scope(scopeString));
    }

    if (!TextUtils.isEmpty(email)) {
      builder.setAccountName(email);
    }

    return builder.build();
  }

  public String getName(Context context) {
    return context.getResources().getString(R.string.idp_name_google);
  }

  @Override public String getProviderId() {
    return GoogleAuthProvider.PROVIDER_ID;
  }

  @Override public void setAuthenticationCallback(IdpCallback callback) {
    idpCallback = callback;
  }

  public void connect() {
    if (googleApiClient != null && !googleApiClient.isConnected()) {
      googleApiClient.connect();
    }
  }

  public void disconnect() {
    if (googleApiClient != null) {
      googleApiClient.disconnect();
      googleApiClient = null;
    }
  }

  private IdpResponse createIdpResponse(GoogleSignInAccount account) {
    return new IdpResponse(GoogleAuthProvider.PROVIDER_ID, account.getEmail(),
        account.getIdToken());
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      if (result != null) {
        if (result.isSuccess()) {
          idpCallback.onSuccess(createIdpResponse(result.getSignInAccount()));
        } else {
          onError(result);
        }
      } else {
        onError("No result found in intent");
      }
    }
  }

  @Override public void startLogin(Controller controller) {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    controller.startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  private void onError(GoogleSignInResult result) {
    Status status = result.getStatus();

    if (status.getStatusCode() == CommonStatusCodes.INVALID_ACCOUNT) {
      googleApiClient.disconnect();
      googleApiClient.connect();
      googleApiClient = new GoogleApiClient.Builder(controller.getActivity())
          .addApi(Auth.GOOGLE_SIGN_IN_API, getSignInOptions(null))
          .build();
      startLogin(controller);
    } else {
      onError(status.getStatusCode() + " " + status.getStatusMessage());
    }
  }

  private void onError(String errorMessage) {
    Log.e(TAG, "Error logging in with Google. " + errorMessage);
    Bundle extra = new Bundle();
    extra.putString(ERROR_KEY, errorMessage);
    idpCallback.onFailure(extra);
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.w(TAG, "onConnectionFailed:" + connectionResult);
    Bundle extra = new Bundle();
    extra.putString(ERROR_KEY, connectionResult.getErrorMessage());
    idpCallback.onFailure(extra);
  }
}

