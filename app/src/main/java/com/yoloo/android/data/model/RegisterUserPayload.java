package com.yoloo.android.data.model;

import java.util.List;

public final class RegisterUserPayload {
  private final String username;
  private final String realname;
  private final String email;
  private final long birthdate;
  private final String countryCode;
  private final String langCode;
  private final String profileImageUrl;
  private final List<String> travelerTypeIds;
  private final String faceebookId;

  public RegisterUserPayload(AccountRealm account) {
    this.realname = account.getRealname();
    this.username = account.getUsername();
    this.email = account.getEmail();
    this.birthdate = account.getBirthdate().getTime();
    this.countryCode = account.getCountry().getCode();
    this.langCode = account.getLangCode();
    this.profileImageUrl = account.getAvatarUrl();
    this.travelerTypeIds = account.getTravelerTypeIds();
    this.faceebookId = account.getFacebookId();
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

  public String getFaceebookId() {
    return faceebookId;
  }
}
