package com.yoloo.android.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class NewsRealm extends RealmObject {

  @PrimaryKey private String id;
  private String title;
  private String bgImageUrl;
  private boolean cover;

  public String getId() {
    return id;
  }

  public NewsRealm setId(String id) {
    this.id = id;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public NewsRealm setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getBgImageUrl() {
    return bgImageUrl;
  }

  public NewsRealm setBgImageUrl(String bgImageUrl) {
    this.bgImageUrl = bgImageUrl;
    return this;
  }

  public boolean isCover() {
    return cover;
  }

  public NewsRealm setCover(boolean cover) {
    this.cover = cover;
    return this;
  }
}
