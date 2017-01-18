package com.yoloo.android.feature.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.bluelinelabs.conductor.Controller;

public interface IdpProvider {
  /**
   * Retrieves the name of the IDP, for display on-screen.
   */
  String getName(Context context);

  String getProviderId();

  void setAuthenticationCallback(IdpCallback callback);

  void onActivityResult(int requestCode, int resultCode, Intent data);

  void startLogin(Controller controller);

  interface IdpCallback {
    void onSuccess(IdpResponse idpResponse);

    void onFailure(Bundle extra);
  }
}
