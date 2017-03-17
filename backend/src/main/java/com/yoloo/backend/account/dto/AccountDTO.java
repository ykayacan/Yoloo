package com.yoloo.backend.account.dto;

import java.util.Date;
import java.util.List;

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
  private String avatarUrl;
  private String bio;
  private String websiteUrl;
  private List<String> interestedCategoryIds;
  private Date created;
  private String locale;
  private boolean isFollowing;
  private long followingCount;
  private long followerCount;
  private long postCount;
  private int level;
  private int pointCount;
  private int bountyCount;
}
