package com.yoloo.android.data.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FcmRealm extends RealmObject {

  @PrimaryKey private String token;
  private boolean pending;

  public FcmRealm() {
  }

  public FcmRealm(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public boolean isPending() {
    return pending;
  }

  public void setPending(boolean pending) {
    this.pending = pending;
  }
}
