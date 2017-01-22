package com.yoloo.android.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FcmRealm extends RealmObject {

  @PrimaryKey
  private String token;
  private boolean pendingChanges;

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

  public boolean isPendingChanges() {
    return pendingChanges;
  }

  public void setPendingChanges(boolean pendingChanges) {
    this.pendingChanges = pendingChanges;
  }
}
