package com.yoloo.android.data.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Response extends RealmObject {

  private static final long STALE_MS = 5 * 1000; // Data is stale after 5 seconds

  private String cursor;
  private String eTag;
  private long timestamp;
  private RealmList<PostRealm> posts;

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

  public String geteTag() {
    return eTag;
  }

  public void seteTag(String eTag) {
    this.eTag = eTag;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public RealmList<PostRealm> getPosts() {
    return posts;
  }

  public void setPosts(RealmList<PostRealm> posts) {
    this.posts = posts;
  }

  public boolean isUpToDate() {
    return System.currentTimeMillis() - timestamp < STALE_MS;
  }
}
