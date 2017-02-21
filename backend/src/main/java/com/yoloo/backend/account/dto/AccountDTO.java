package com.yoloo.backend.account.dto;

import java.util.Date;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Builder
public class AccountDTO {

  private String id;
  @Wither private String username;
  @Wither private String realname;
  @Wither private String email;
  @Wither private String avatarUrl;
  private Date created;
  private String locale;
  @Wither private boolean isFollowing;
  @Wither private long followingCount;
  @Wither private long followerCount;
  @Wither private long postCount;
  @Wither private int level;
  @Wither private int pointCount;
  @Wither private int bountyCount;
}
