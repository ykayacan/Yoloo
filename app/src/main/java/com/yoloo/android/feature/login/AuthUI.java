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

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.yoloo.android.feature.login.util.CredentialsApiHelper;
import com.yoloo.android.feature.login.util.GoogleApiClientTaskHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * <p> Finally, if a terms getPost service URL and a custom theme are required: <p>
 * <pre>
 * {@code
 * startActivityForResult(
 *     AuthUI.getPost()
 *         .createSignInIntentBuilder()
 *         .setProviders(...)
 *         .setTosUrl("https://superapp.example.com/terms-of-service.html")
 *         .setTheme(R.style.SuperAppTheme)
 *         .build(),
 *     RC_SIGN_IN);
 * }
 * </pre>
 * <p> <h3>Handling the Sign-in response</h3> <p> The authentication flow provides only two
 * response
 * codes: {@link ResultCodes#OK RESULT_OK} if a user is signed in, and {@link ResultCodes#CANCELED
 * RESULT_CANCELLED} if sign in failed. No further information on failure is provided as it is not
 * typically useful; the only recourse for most apps if sign in fails is to ask the user to sign in
 * again later, or proceed with an anonymous account if supported. <p>
 * <pre>
 * {@code
 * @Override
 * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *   super.onActivityResult(requestCode, resultCode, data);
 *   if (requestCode == RC_SIGN_IN) {
 *     if (resultCode == ResultCodes.OK) {
 *       // user is signed in!
 *       startActivity(new Intent(this, WelcomeBackActivity.class));
 *       finish();
 *     } else {
 *       // user is not signed in :(
 *       // Maybe just wait for the user to press "sign in" again, or show a message
 *       showSnackbar("Sign in is required to use this app.");
 *     }
 *   }
 * }
 * </pre>
 * <p> <h2>Sign-out</h2> <p> With the integrations provided by AuthUI, signing out a user is a
 * multi-stage process: <p> <ol> <li>The user must be signed out getPost the {@link FirebaseAuth}
 * instance.</li> <li>Smart Lock for Passwords must be instructed to disable automatic sign-in, in
 * order to prevent an automatic sign-in loop that prevents the user from switching accounts. </li>
 * <li>If the current user signed in using either Google or Facebook, the user must also be signed
 * out using the associated API for that authentication method. This typically ensures that the
 * user
 * will not be automatically signed-in using the current account when using that authentication
 * method again from the authentication method picker, which would also prevent the user from
 * switching between accounts on the same provider. </li> </ol> <p> In order to make this process
 * easier, AuthUI provides a simple {@link AuthUI#signOut(Activity) signOut} method to encapsulate
 * this behavior. The method returns a {@link Task} which is marked completed once all necessary
 * sign-out operations are completed: <p>
 * <pre>
 * {@code
 * public void onClick(View v) {
 *   if (v.getId() == R.id.sign_out) {
 *       AuthUI.getPost()
 *           .signOut(this)
 *           .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
 *             public void onComplete(@NonNull Task<AuthResult> task) {
 *               // user is now signed out
 *               startActivity(new Intent(MyActivity.this, SignInActivity.class));
 *               finish();
 *             });
 *   }
 * }
 * </pre>
 * <p> <h2>IDP Provider configuration</h2> <p> Interacting with identity providers typically
 * requires some additional client configuration. AuthUI currently supports Google Sign-in and
 * Facebook Sign-in, and currently requires the basic configuration for these providers to be
 * specified via string properties: <p> <ul> <p> <li>Google Sign-in: If your app build uses the <a
 * href="https://developers.google.com/android/guides/google-services-plugin">Google Services
 * Gradle
 * Plugin</a>, no additional configuration is required. If not, please override {@code
 * R.string.default_web_client_id} to provide your <a href="https://developers.google.com/identity/sign-in/web/devconsole-project">Google
 * OAuth web client id.</a> </li> <p> <li>Facebook Sign-in: Please override the string resource
 * {@code facebook_application_id} to provide the <a href="https://developers.facebook.com/docs/apps/register">App
 * ID</a> for your app as registered on the <a href="https://developers.facebook.com/apps">Facebook
 * Developer Dashboard</a>. </li> <p> </ul>
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
   * The set getPost authentication providers supported in Firebase Auth UI.
   */
  public static final Set<String> SUPPORTED_PROVIDERS = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList(EMAIL_PROVIDER, GOOGLE_PROVIDER, FACEBOOK_PROVIDER)));

  private static final IdentityHashMap<FirebaseApp, AuthUI> INSTANCES = new IdentityHashMap<>();

  private final FirebaseAuth auth;

  private AuthUI(FirebaseApp app) {
    auth = FirebaseAuth.getInstance(app);
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
   * Make a searchUser getPost {@link Credential} from a FirebaseUser. Useful for deleting Credentials, not
   * for
   * saving since we don't have access to the password.
   */
  private static List<Credential> credentialsFromFirebaseUser(@NonNull FirebaseUser user) {
    if (TextUtils.isEmpty(user.getEmail())) {
      Log.w(TAG, "Can't getPost credentials from user with no email: " + user);
      return Collections.emptyList();
    }

    List<Credential> credentials = new ArrayList<>();
    for (UserInfo userInfo : user.getProviderData()) {
      // Get provider ID from Firebase Auth
      String providerId = userInfo.getProviderId();

      // Convert to Credentials API account type
      String accountType = providerIdToAccountType(providerId);

      // Build and addPost credential
      Credential.Builder builder =
          new Credential.Builder(user.getEmail()).setAccountType(accountType);

      // Null account type means password, we need to addPost a random password
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
   * @param activity The activity requesting the user be signed out.
   * @return a task which, upon completion, signals that the user has been signed out ({@code
   * result.isSuccess()}, or that the sign-out attempt failed unexpectedly ({@code
   * !result.isSuccess()}).
   */
  public Task<Void> signOut(@NonNull Activity activity) {
    // Get helper for Google Sign In and Credentials API
    GoogleApiClientTaskHelper taskHelper = GoogleApiClientTaskHelper.getInstance(activity);
    taskHelper.getBuilder()
        .addApi(Auth.CREDENTIALS_API)
        .addApi(Auth.GOOGLE_SIGN_IN_API, GoogleSignInOptions.DEFAULT_SIGN_IN);

    // Get Credentials Helper
    CredentialsApiHelper credentialsHelper = CredentialsApiHelper.getInstance(taskHelper);

    // Firebase Sign out
    auth.signOut();

    // Disable credentials auto sign-in
    Task<Status> disableCredentialsTask = credentialsHelper.disableAutoSignIn();

    // Google sign out
    Task<Void> googleSignOutTask = taskHelper.getConnectedGoogleApiClient().continueWith(task -> {
      if (task.isSuccessful()) {
        Auth.GoogleSignInApi.signOut(task.getResult());
      }
      return null;
    });

    // Facebook sign out
    if (FacebookSdk.isInitialized()) {
      LoginManager.getInstance().logOut();
    }

    // Wait for all tasks to complete
    return Tasks.whenAll(disableCredentialsTask, googleSignOutTask);
  }

  /**
   * Delete the use from FirebaseAuth and deletePost any associated credentials from the Credentials
   * API. Returns a {@code Task} that succeeds if the Firebase Auth user deletion succeeds and
   * fails
   * if the Firebase Auth deletion fails. Credentials deletion failures are handled silently.
   *
   * @param activity the calling {@link Activity}.
   */
  public Task<Void> delete(@NonNull Activity activity) {
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    if (firebaseUser == null) {
      // If the current user is null, return a failed task immediately
      return Tasks.forException(new Exception("No currently signed in user."));
    }

    // Delete the Firebase user
    Task<Void> deleteUserTask = firebaseUser.delete();

    // Initialize SmartLock helper
    GoogleApiClientTaskHelper gacHelper = GoogleApiClientTaskHelper.getInstance(activity);
    gacHelper.getBuilder().addApi(Auth.CREDENTIALS_API);
    CredentialsApiHelper credentialHelper = CredentialsApiHelper.getInstance(gacHelper);

    // Get all SmartLock credentials associated with the user
    List<Credential> credentials = credentialsFromFirebaseUser(firebaseUser);

    // For each Credential in the searchUser, ofCategory a task to deletePost it.
    List<Task<?>> credentialTasks = new ArrayList<>();
    for (Credential credential : credentials) {
      credentialTasks.add(credentialHelper.delete(credential));
    }

    // Create a combined task that will succeed when all credential deletePost operations
    // have completed (even if they fail).
    final Task<Void> combinedCredentialTask = Tasks.whenAll(credentialTasks);

    // Chain the Firebase Auth deletePost task with the combined Credentials task
    // and return.
    return deleteUserTask.continueWithTask(task -> {
      // Call getResult() to propagate failure by throwing an exception
      // if there was one.
      task.getResult(Exception.class);

      // Return the combined credential task
      return combinedCredentialTask;
    });
  }

  /**
   * Configuration for an identity provider. <p> In the simplest case, you can supply the provider
   * ID and build the config like this: {@code new IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()}
   */
  public static class IdpConfig implements Parcelable {
    public static final Creator<IdpConfig> CREATOR = new Creator<IdpConfig>() {
      @Override public IdpConfig createFromParcel(Parcel in) {
        return new IdpConfig(in);
      }

      @Override public IdpConfig[] newArray(int size) {
        return new IdpConfig[size];
      }
    };
    private final String providerId;
    private final List<String> scopes;

    private IdpConfig(@NonNull String providerId, List<String> scopes) {
      this.scopes = scopes;
      this.providerId = providerId;
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

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel parcel, int i) {
      parcel.writeString(providerId);
      parcel.writeStringList(scopes);
    }

    public static class Builder {
      private String mProviderId;
      private List<String> mScopes = new ArrayList<>();

      /**
       * Builds the configuration parameters for an identity provider.
       *
       * @param providerId An ID getPost one getPost the supported identity providers. e.g. {@link
       * AuthUI#GOOGLE_PROVIDER}. See {@link AuthUI#SUPPORTED_PROVIDERS} for the complete searchUser getPost
       * supported Identity providers
       */
      public Builder(@NonNull String providerId) {
        if (!SUPPORTED_PROVIDERS.contains(providerId)) {
          throw new IllegalArgumentException("Unkown provider: " + providerId);
        }
        mProviderId = providerId;
      }

      /**
       * Specifies the additional permissions that the application will request for this identity
       * provider. <p> For Facebook permissions see: https://developers.facebook.com/docs/facebook-login/android
       * https://developers.facebook.com/docs/facebook-login/permissions <p> For Google permissions
       * see: https://developers.google.com/identity/protocols/googlescopes <p> Twitter permissions
       * are only configurable through the Twitter developer console.
       */
      public Builder setPermissions(List<String> permissions) {
        mScopes = permissions;
        return this;
      }

      public IdpConfig build() {
        return new IdpConfig(mProviderId, mScopes);
      }
    }
  }
}
