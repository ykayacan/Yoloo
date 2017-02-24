package com.yoloo.android.data.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class CategoryRealm extends RealmObject {

  @PrimaryKey
  private String id;
  @Index
  private String name;
  private String backgroundUrl;
  private long posts;
  private double rank;
  @Index
  private String type;

  public String getId() {
    return id;
  }

  public CategoryRealm setId(String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public CategoryRealm setName(String name) {
    this.name = name;
    return this;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public CategoryRealm setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
    return this;
  }

  public long getPosts() {
    return posts;
  }

  public CategoryRealm setPosts(long posts) {
    this.posts = posts;
    return this;
  }

  public double getRank() {
    return rank;
  }

  public CategoryRealm setRank(double rank) {
    this.rank = rank;
    return this;
  }

  public String getType() {
    return type;
  }

  public CategoryRealm setType(String type) {
    this.type = type;
    return this;
  }

  @Override public String toString() {
    return "CategoryRealm{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", backgroundUrl='" + backgroundUrl + '\'' +
        ", postCount=" + posts +
        ", rank=" + rank +
        ", type='" + type + '\'' +
        '}';
  }
}
