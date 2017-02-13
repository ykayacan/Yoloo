package com.yoloo.android.data.model.firebase;

public class ChatUser {

  public static final int ROLE_USER = 1;
  public static final int ROLE_MODERATOR = 2;
  public static final int ROLE_ADMIN = 3;

  private final String userId;
  private final int userRole;

  public ChatUser(String userId, int userRole) {
    this.userId = userId;
    this.userRole = userRole;
  }

  public String getUserId() {
    return userId;
  }

  public int getUserRole() {
    return userRole;
  }
}
