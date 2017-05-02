package com.yoloo.backend.account.dto;

import com.yoloo.backend.country.Country;
import java.util.Date;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountDTO {
  private String id;
  private String username;
  private String realname;
  private String email;
  private String gender;
  private int age;
  private String avatarUrl;
  private String bio;
  private String websiteUrl;
  private String langCode;
  private CountryDTO country;
  private Set<CountryDTO> visitedCountries;
  private Set<String> subscribedGroupIds;
  private Date created;
  private boolean isFollowing;
  private long followingCount;
  private long followerCount;
  private long postCount;
  private String levelTitle;
  private int level;
  private int pointCount;
  private int bountyCount;

  @Value
  public static class CountryDTO {
    private String countryCode;
    private String countryName;
    private String flagUrl;

    public CountryDTO(Country country) {
      this.countryCode = country.getId();
      this.countryName = country.getName();
      this.flagUrl = country.getFlagUrl().getValue();
    }
  }
}
