package com.yoloo.android.data.model;

import java.util.List;

public final class RegisterUserPayload {
  private String username;
  private String realname;
  private String email;
  private long birthdate;
  private String countryCode;
  private String langCode;
  private String profileImageUrl;
  private List<String> travelerTypeIds;

  public RegisterUserPayload(AccountRealm account) {
    this.realname = account.getRealname();
    this.username = account.getUsername();
    this.email = account.getEmail();
    this.birthdate = account.getBirthdate().getTime();
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

  public long getBirthdate() {
    return birthdate;
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
