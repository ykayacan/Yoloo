package com.yoloo.android.notificationhandler;

import com.squareup.moshi.Json;

public class NotificationResponse {
  public static String KEY_ACTION = "action";
  public static String KEY_SENDER_AVATAR_URL = "sAvatarUrl";
  public static String KEY_SENDER_USERNAME = "sUsername";
  public static String KEY_POST_ID = "postId";
  public static String KEY_ACCEPTED_ID = "acceptedId";
  public static String KEY_COMMENT = "comment";
  public static String KEY_BOUNTIES = "bounties";
  public static String KEY_POINTS = "points";
  public static String KEY_GAME_ACTION = "gameAction";
  public static String KEY_LEVEL = "level";
  public static String KEY_USER_ID = "userId";
  public static String KEY_CHAT_ID = "chatId";

  @Json(name = "action") private String action;
  @Json(name = "sAvatarUrl") private String senderAvatarUrl;
  @Json(name = "sUsername") private String senderUsername;
  @Json(name = "postId") private String postId;
  @Json(name = "acceptedId") private String acceptedCommentId;
  @Json(name = "comment") private String comment;
  @Json(name = "bounties") private String bounties;
  @Json(name = "points") private String points;
  @Json(name = "gameAction") private String gameAction;
  @Json(name = "level") private String level;
  @Json(name = "userId") private String userId;

  public String getAction() {
    return action;
  }

  public String getSenderAvatarUrl() {
    return senderAvatarUrl;
  }

  public String getSenderUsername() {
    return senderUsername;
  }

  public String getPostId() {
    return postId;
  }

  public String getAcceptedCommentId() {
    return acceptedCommentId;
  }

  public String getComment() {
    return comment;
  }

  public String getBounties() {
    return bounties;
  }

  public String getPoints() {
    return points;
  }

  public String getGameAction() {
    return gameAction;
  }

  public String getLevel() {
    return level;
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public String toString() {
    return "NotificationResponse{"
        + "action='"
        + action
        + '\''
        + ", senderAvatarUrl='"
        + senderAvatarUrl
        + '\''
        + ", senderUsername='"
        + senderUsername
        + '\''
        + ", postId='"
        + postId
        + '\''
        + ", acceptedCommentId='"
        + acceptedCommentId
        + '\''
        + ", comment='"
        + comment
        + '\''
        + ", bounties='"
        + bounties
        + '\''
        + ", points='"
        + points
        + '\''
        + ", gameAction='"
        + gameAction
        + '\''
        + ", level='"
        + level
        + '\''
        + ", userId='"
        + userId
        + '\''
        + '}';
  }
}
