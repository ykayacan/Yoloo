package com.yoloo.android.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.Objects;

public class MediaRealm extends RealmObject {

  @PrimaryKey
  private String id;

  private AccountRealm account;

  private String url;

  public String getId() {
    return id;
  }

  public MediaRealm setId(String id) {
    this.id = id;
    return this;
  }

  public AccountRealm getAccount() {
    return account;
  }

  public MediaRealm setAccount(AccountRealm account) {
    this.account = account;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public MediaRealm setUrl(String url) {
    this.url = url;
    return this;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MediaRealm that = (MediaRealm) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(account, that.account) &&
        Objects.equals(url, that.url);
  }

  @Override public int hashCode() {
    return Objects.hash(id, account, url);
  }

  @Override public String toString() {
    return "MediaRealm{" +
        "id='" + id + '\'' +
        ", account=" + account +
        ", url='" + url + '\'' +
        '}';
  }
}
