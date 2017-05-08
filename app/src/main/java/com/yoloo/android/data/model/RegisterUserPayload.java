package com.yoloo.android.data.model;

import java.util.List;

public final class RegisterUserPayload {
  private final String username;
  private final String realname;
  private final String email;
  private final long birthday;
  private final String countryCode;
  private final String langCode;
  private final String profileImageUrl;
  private final List<String> travelerTypeIds;

  public RegisterUserPayload(AccountRealm account) {
    this.realname = account.getRealname();
    this.username = account.getUsername();
    this.email = account.getEmail();
    this.birthday = account.getBirthdate().getTime();
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

  public long getBirthday() {
    return birthday;
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
