package com.yoloo.android.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Meta extends RealmObject {

  public static final String META_FEED = "meta_feed";

  @PrimaryKey String id;
  private String cursor;
  private String eTag;
  private long timestamp;

  public String getId() {
    return id;
  }

  public Meta setId(String id) {
    this.id = id;
    return this;
  }

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
}
