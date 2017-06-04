package com.yoloo.android.notificationhandler;

import com.squareup.moshi.Json;

public class NotificationResponse {
  public static final String KEY_ACTION = "action";
  public static final String KEY_SENDER_AVATAR_URL = "sAvatarUrl";
  public static final String KEY_SENDER_USERNAME = "sUsername";
  public static final String KEY_POST_ID = "postId";
  public static final String KEY_ACCEPTED_ID = "acceptedId";
  public static final String KEY_COMMENT = "comment";
  public static final String KEY_BOUNTIES = "bounties";
  public static final String KEY_POINTS = "points";
  public static final String KEY_GAME_ACTION = "gameAction";
  public static final String KEY_LEVEL = "level";
  public static final String KEY_USER_ID = "userId";
  public static final String KEY_CHAT_ID = "chatId";
  public static final String GROUP_NAME = "groupName";

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
  @Json(name = "groupName") private String groupName;

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

  public String getGroupName() {
    return groupName;
  }

  @Override public String toString() {
    return "NotificationResponse{" +
        "action='" + action + '\'' +
        ", senderAvatarUrl='" + senderAvatarUrl + '\'' +
        ", senderUsername='" + senderUsername + '\'' +
        ", postId='" + postId + '\'' +
        ", acceptedCommentId='" + acceptedCommentId + '\'' +
        ", comment='" + comment + '\'' +
        ", bounties='" + bounties + '\'' +
        ", points='" + points + '\'' +
        ", gameAction='" + gameAction + '\'' +
        ", level='" + level + '\'' +
        ", userId='" + userId + '\'' +
        ", groupName='" + groupName + '\'' +
        '}';
  }
}
