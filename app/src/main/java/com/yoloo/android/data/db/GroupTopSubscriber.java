package com.yoloo.android.data.db;

import com.yoloo.android.util.Objects;
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

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GroupTopSubscriber that = (GroupTopSubscriber) o;
    return Objects.equal(id, that.id) &&
        Objects.equal(avatarUrl, that.avatarUrl);
  }

  @Override public int hashCode() {
    return Objects.hashCode(id, avatarUrl);
  }

  @Override public String toString() {
    return "GroupTopSubscriber{" +
        "id='" + id + '\'' +
        ", avatarUrl='" + avatarUrl + '\'' +
        '}';
  }
}
