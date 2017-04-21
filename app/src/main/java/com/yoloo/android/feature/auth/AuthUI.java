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

package com.yoloo.android.feature.auth;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.yoloo.android.feature.auth.util.CredentialTaskApi;
import com.yoloo.android.feature.auth.util.GoogleSignInHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import timber.log.Timber;

/**
 * The entry point to the AuthUI authentication flow, and related utility methods. If your
 * application uses the default {@link FirebaseApp} instance, an AuthUI instance can be retrieved
 * simply by calling {@link AuthUI#getInstance()}. If an alternative app instance is in use, call
 * {@link AuthUI#getInstance(FirebaseApp)} instead, passing the appropriate app instance.
 * <p>
 * <p>
 * See the <a href="https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#table-of-contents">README</a>
 * for examples on how to get started with FirebaseUI Auth.
 */
public class AuthUI {

  /**
   * Provider identifier for email and password credentials.
   */
  public static final String EMAIL_PROVIDER = EmailAuthProvider.PROVIDER_ID;

  /**
   * Provider identifier for Google.
   */
  public static final String GOOGLE_PROVIDER = GoogleAuthProvider.PROVIDER_ID;

  /**
   * Provider identifier for Facebook.
   */
  public static final String FACEBOOK_PROVIDER = FacebookAuthProvider.PROVIDER_ID;

  /**
   * The set of authentication providers supported in Firebase Auth UI.
   */
  public static final Set<String> SUPPORTED_PROVIDERS = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList(EMAIL_PROVIDER, GOOGLE_PROVIDER, FACEBOOK_PROVIDER)));

  private static final IdentityHashMap<FirebaseApp, AuthUI> INSTANCES = new IdentityHashMap<>();

  private final FirebaseApp app;
  private final FirebaseAuth auth;

  private AuthUI(FirebaseApp app) {
    this.app = app;
    this.auth = FirebaseAuth.getInstance(this.app);
  }

  /**
   * Retrieves the {@link AuthUI} instance associated with the default app, as returned by {@code
   * FirebaseApp.getPost()}.
   *
   * @throws IllegalStateException if the default app is not initialized.
   */
  public static AuthUI getInstance() {
    return getInstance(FirebaseApp.getInstance());
  }

  /**
   * Retrieves the {@link AuthUI} instance associated the the specified app.
   */
  public static AuthUI getInstance(FirebaseApp app) {
    AuthUI authUi;
    synchronized (INSTANCES) {
      authUi = INSTANCES.get(app);
      if (authUi == null) {
        authUi = new AuthUI(app);
        INSTANCES.put(app, authUi);
      }
    }
    return authUi;
  }

  /**
   * Make a searchUser getPost {@link Credential} from a FirebaseUser.
   * Useful for deleting Credentials, not
   * for saving since we don't have access to the password.
   */
  private static List<Credential> credentialsFromFirebaseUser(@NonNull FirebaseUser user) {
    if (TextUtils.isEmpty(user.getEmail())) {
      Timber.w("Can't getPost credentials from user with no email: %s", user);
      return Collections.emptyList();
    }

    List<Credential> credentials = new ArrayList<>();
    for (UserInfo userInfo : user.getProviderData()) {
      // Get provider ID from Firebase Auth
      String providerId = userInfo.getProviderId();

      // Convert to Credentials API account type
      String accountType = providerIdToAccountType(providerId);

      // Build and addPostToBeginning credential
      Credential.Builder builder =
          new Credential.Builder(user.getEmail()).setAccountType(accountType);

      // Null account type means password, we need to addPostToBeginning a random password
      // to make deletion succeed.
      if (accountType == null) {
        builder.setPassword("some_password");
      }

      credentials.add(builder.build());
    }

    return credentials;
  }

  /**
   * Translate a Firebase Auth provider ID (such as {@link GoogleAuthProvider#PROVIDER_ID}) to a
   * Credentials API account type (such as {@link IdentityProviders#GOOGLE}).
   */
  private static String providerIdToAccountType(@NonNull String providerId) {
    switch (providerId) {
      case GoogleAuthProvider.PROVIDER_ID:
        return IdentityProviders.GOOGLE;
      case FacebookAuthProvider.PROVIDER_ID:
        return IdentityProviders.FACEBOOK;
      case EmailAuthProvider.PROVIDER_ID:
        // The account type for email/password creds is null
        return null;
      default:
        return null;
    }
  }

  /**
   * Signs the current user out, if one is signed in.
   *
   * @param activity the activity requesting the user be signed out
   * @return A task which, upon completion, signals that the user has been signed out ({@link
   * Task#isSuccessful()}, or that the sign-out attempt failed unexpectedly !{@link
   * Task#isSuccessful()}).
   */
  public Task<Void> signOut(@NonNull FragmentActivity activity) {
    // Get Credentials Helper
    GoogleSignInHelper credentialsHelper = GoogleSignInHelper.getInstance(activity);

    // Firebase Sign out
    auth.signOut();

    // Disable credentials auto sign-in
    Task<Status> disableCredentialsTask = credentialsHelper.disableAutoSignIn();

    // Google sign out
    Task<Status> signOutTask = credentialsHelper.signOut();

    // Facebook sign out
    LoginManager.getInstance().logOut();

    // Wait for all tasks to complete
    return Tasks.whenAll(disableCredentialsTask, signOutTask);
  }

  /**
   * Delete the use from FirebaseAuth and delete any associated credentials from the Credentials
   * API. Returns a {@link Task} that succeeds if the Firebase Auth user deletion succeeds and
   * fails if the Firebase Auth deletion fails. Credentials deletion failures are handled
   * silently.
   *
   * @param activity the calling {@link Activity}.
   */
  public Task<Void> delete(@NonNull FragmentActivity activity) {
    // Initialize SmartLock helper
    CredentialTaskApi credentialHelper = GoogleSignInHelper.getInstance(activity);

    return getDeleteTask(credentialHelper);
  }

  private Task<Void> getDeleteTask(CredentialTaskApi credentialHelper) {
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    if (firebaseUser == null) {
      // If the current user is null, return a failed task immediately
      return Tasks.forException(new Exception("No currently signed in user."));
    }

    // Delete the Firebase user
    return firebaseUser.delete();
  }

  /**
   * Configuration for an identity provider. <p> In the simplest case, you can supply the provider
   * ID and build the config like this:
   * {@code new IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()}
   */
  public static class IdpConfig implements Parcelable {
    public static final Creator<IdpConfig> CREATOR = new Creator<IdpConfig>() {
      @Override
      public IdpConfig createFromParcel(Parcel in) {
        return new IdpConfig(in);
      }

      @Override
      public IdpConfig[] newArray(int size) {
        return new IdpConfig[size];
      }
    };
    private final String providerId;
    private final List<String> scopes;

    private IdpConfig(@NonNull String providerId, List<String> scopes) {
      this.providerId = providerId;
      this.scopes = scopes;
    }

    private IdpConfig(Parcel in) {
      providerId = in.readString();
      scopes = in.createStringArrayList();
    }

    public String getProviderId() {
      return providerId;
    }

    public IdpConfig addScope(String scope) {
      scopes.add(scope);
      return this;
    }

    public List<String> getScopes() {
      return scopes;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
      parcel.writeString(providerId);
      parcel.writeStringList(scopes);
    }

    public static class Builder {
      private String providerId;
      private List<String> scopes = new ArrayList<>();

      /**
       * Builds the configuration parameters for an identity provider.
       *
       * @param providerId An ID getPost one getPost the supported identity providers.
       * e.g. {@link AuthUI#GOOGLE_PROVIDER}.
       * See {@link AuthUI#SUPPORTED_PROVIDERS} for the complete searchUser getPost
       * supported Identity providers
       */
      public Builder(@NonNull String providerId) {
        if (!SUPPORTED_PROVIDERS.contains(providerId)) {
          throw new IllegalArgumentException("Unkown provider: " + providerId);
        }
        this.providerId = providerId;
      }

      /**
       * Specifies the additional permissions that the application will request for this identity
       * provider. <p> For Facebook permissions see:
       * https://developers.facebook.com/docs/facebook-login/android
       * https://developers.facebook.com/docs/facebook-login/permissions <p> For Google permissions
       * see: https://developers.google.com/identity/protocols/googlescopes <p> Twitter permissions
       * are only configurable through the Twitter developer console.
       */
      public Builder setPermissions(List<String> permissions) {
        scopes = permissions;
        return this;
      }

      public IdpConfig build() {
        return new IdpConfig(providerId, scopes);
      }
    }
  }
}
