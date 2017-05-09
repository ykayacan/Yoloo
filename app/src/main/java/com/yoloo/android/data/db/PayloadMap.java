package com.yoloo.android.data.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.Objects;

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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PayloadMap)) return false;
    PayloadMap map = (PayloadMap) o;
    return Objects.equals(getKey(), map.getKey()) && Objects.equals(getValue(), map.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getKey(), getValue());
  }

  @Override
  public String toString() {
    return "PayloadMap{" + "key='" + key + '\'' + ", value='" + value + '\'' + '}';
  }
}
