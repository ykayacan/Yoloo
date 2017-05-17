package com.yoloo.android.data.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GroupTopSubscriber extends RealmObject {

  private @PrimaryKey String id;
  private String avatarUrl;

  public GroupTopSubscriber() {
  }

  public GroupTopSubscriber(String id, String avatarUrl) {
    this.id = id;
    this.avatarUrl = avatarUrl;
  }

  public String getId() {
    return id;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }
}
