package com.yoloo.android.data.db;

import java.util.List;

public final class RegisterUserPayload {
  private final String username;
  private final String realname;
  private final String email;
  private final String countryCode;
  private final String langCode;
  private final String profileImageUrl;
  private final List<String> travelerTypeIds;

  public RegisterUserPayload(AccountRealm account) {
    this.realname = account.getRealname();
    this.username = account.getUsername();
    this.email = account.getEmail();
    this.countryCode = account.getCountry().getCode();
    this.langCode = account.getLangCode();
    this.profileImageUrl = account.getAvatarUrl();
    this.travelerTypeIds = account.getTravelerTypeIds();
  }

  public String getUsername() {
    return username;
  }

  public String getRealname() {
    return realname;
  }

  public String getEmail() {
    return email;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public String getLangCode() {
    return langCode;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public List<String> getTravelerTypeIds() {
    return travelerTypeIds;
  }
}
