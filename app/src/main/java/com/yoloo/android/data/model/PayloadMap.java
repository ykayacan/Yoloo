package com.yoloo.android.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PayloadMap extends RealmObject {
  @PrimaryKey private String key;
  private String value;

  public PayloadMap(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public PayloadMap() {
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "PayloadMap{" + "key='" + key + '\'' + ", value='" + value + '\'' + '}';
  }
}
