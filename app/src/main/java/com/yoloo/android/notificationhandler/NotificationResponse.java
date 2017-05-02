package com.yoloo.android.notificationhandler;

import com.squareup.moshi.Json;

public class NotificationResponse {
  @Json(name = "act") private String action;
  @Json(name = "ga") private String gameAction;
  @Json(name = "bounties") private String bounties;
  @Json(name = "points") private String points;
  @Json(name = "sU") private String senderUsername;
  @Json(name = "level") private String level;

  public String getAction() {
    return action;
  }

  public String getGameAction() {
    return gameAction;
  }

  public String getBounties() {
    return bounties;
  }

  public String getPoints() {
    return points;
  }

  public String getSenderUsername() {
    return senderUsername;
  }

  public String getLevel() {
    return level;
  }
}
