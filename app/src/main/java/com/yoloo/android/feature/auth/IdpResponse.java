package com.yoloo.android.feature.auth;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A container that encapsulates the result from authenticating with an Identity Provider.
 */
public class IdpResponse implements Parcelable {

  public static final Creator<IdpResponse> CREATOR = new Creator<IdpResponse>() {
    @Override
    public IdpResponse createFromParcel(Parcel in) {
      return new IdpResponse(in.readString(), in.readString(), in.readString(), in.readString(),
          in.readString(), in.readString(), in.readInt());
    }

    @Override
    public IdpResponse[] newArray(int size) {
      return new IdpResponse[size];
    }
  };

  private final String providerId;
  private final String id;
  private final String email;
  private final String token;
  private final String name;
  private final String pictureUrl;
  private final int errorCode;

  public IdpResponse(int errorCode) {
    this(null, null, null, null, null, null, errorCode);
  }

  public IdpResponse(@NonNull String providerId, @NonNull String email, @NonNull String token,
      @NonNull String name, @NonNull String pictureUrl) {
    this(providerId, null, email, token, name, pictureUrl, ResultCodes.OK);
  }

  public IdpResponse(@NonNull String providerId, @NonNull String id /* Facebook only */,
      @NonNull String email, @NonNull String token, @NonNull String name,
      @NonNull String pictureUrl) {
    this(providerId, id, email, token, name, pictureUrl, ResultCodes.OK);
  }

  private IdpResponse(String providerId, String id, String email, String token, String name,
      String pictureUrl, int errorCode) {
    this.providerId = providerId;
    this.id = id;
    this.email = email;
    this.token = token;
    this.name = name;
    this.pictureUrl = pictureUrl;
    this.errorCode = errorCode;
  }

  /**
   * Extract the {@link IdpResponse} from the flow's result intent.
   *
   * @param resultIntent The intent which {@code onActivityResult} was called with.
   * @return The IdpResponse containing the token(s) from signing in with the Idp
   */
  @Nullable
  public static IdpResponse fromResultIntent(Intent resultIntent) {
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
   * Get the type from provider. e.g.
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
   * Get the token received as a result from logging in with the specified IDP
   */
  @Nullable
  public String getIdpToken() {
    return token;
  }

  public String getName() {
    return name;
  }

  public String getPictureUrl() {
    return pictureUrl;
  }

  @Nullable
  public String getId() {
    return id;
  }

  /**
   * Get the error code for a failed sign in
   */
  public int getErrorCode() {
    return errorCode;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(providerId);
    dest.writeString(id);
    dest.writeString(email);
    dest.writeString(token);
    dest.writeString(name);
    dest.writeString(pictureUrl);
    dest.writeInt(errorCode);
  }

  @Override
  public String toString() {
    return "IdpResponse{"
        + "providerId='"
        + providerId
        + '\''
        + ", id='"
        + id
        + '\''
        + ", email='"
        + email
        + '\''
        + ", token='"
        + token
        + '\''
        + ", name='"
        + name
        + '\''
        + ", pictureUrl='"
        + pictureUrl
        + '\''
        + ", errorCode="
        + errorCode
        + '}';
  }
}
