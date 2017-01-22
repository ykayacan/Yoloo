package com.yoloo.android.feature.login;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A container that encapsulates the result of authenticating with an Identity Provider.
 */
public class IdpResponse implements Parcelable {

  public static final Creator<IdpResponse> CREATOR = new Creator<IdpResponse>() {
    @Override public IdpResponse createFromParcel(Parcel in) {
      return new IdpResponse(in.readString(), in.readString(), in.readString(), in.readString(),
          in.readInt());
    }

    @Override public IdpResponse[] newArray(int size) {
      return new IdpResponse[size];
    }
  };

  private final String providerId;
  private final String email;
  private final String token;
  private final String secret;
  private final int errorCode;

  private IdpResponse(int errorCode) {
    this(null, null, null, null, errorCode);
  }

  public IdpResponse(@NonNull String providerId, @NonNull String email) {
    this(providerId, email, null, null, ResultCodes.OK);
  }

  public IdpResponse(@NonNull String providerId, @NonNull String email, @NonNull String token) {
    this(providerId, email, token, null, ResultCodes.OK);
  }

  public IdpResponse(@NonNull String providerId, @NonNull String email, @NonNull String token,
      @NonNull String secret) {
    this(providerId, email, token, secret, ResultCodes.OK);
  }

  private IdpResponse(String providerId, String email, String token, String secret, int errorCode) {
    this.providerId = providerId;
    this.email = email;
    this.token = token;
    this.secret = secret;
    this.errorCode = errorCode;
  }

  /**
   * Extract the {@link IdpResponse} from the flow's result intent.
   *
   * @param resultIntent The intent which {@code onActivityResult} was called with.
   * @return The IdpResponse containing the token(s) from signing in with the Idp
   */
  @Nullable public static IdpResponse fromResultIntent(Intent resultIntent) {
    if (resultIntent != null) {
      return resultIntent.getParcelableExtra(ExtraConstants.EXTRA_IDP_RESPONSE);
    } else {
      return null;
    }
  }

  public static Intent getIntent(IdpResponse response) {
    return new Intent().putExtra(ExtraConstants.EXTRA_IDP_RESPONSE, response);
  }

  public static Intent getErrorCodeIntent(int errorCode) {
    return getIntent(new IdpResponse(errorCode));
  }

  /**
   * Get the type of provider. e.g.
   */
  public String getProviderType() {
    return providerId;
  }

  /**
   * Get the email used to sign in.
   */
  public String getEmail() {
    return email;
  }

  /**
   * Get the token received as a result of logging in with the specified IDP
   */
  @Nullable public String getIdpToken() {
    return token;
  }

  /**
   * Twitter only. Return the token secret received as a result of logging in with Twitter.
   */
  @Nullable public String getIdpSecret() {
    return secret;
  }

  /**
   * Get the error code for a failed sign in
   */
  public int getErrorCode() {
    return errorCode;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(providerId);
    dest.writeString(email);
    dest.writeString(token);
    dest.writeString(secret);
    dest.writeInt(errorCode);
  }
}
