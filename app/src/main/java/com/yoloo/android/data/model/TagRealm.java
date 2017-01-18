package com.yoloo.android.data.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class TagRealm extends RealmObject {

  @PrimaryKey
  private String id;
  private String name;
  private String language;
  @Index
  private long posts;
  @Index
  private boolean isRecommended;

  public String getId() {
    return id;
  }

  public TagRealm setId(String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public TagRealm setName(String name) {
    this.name = name;
    return this;
  }

  public String getLanguage() {
    return language;
  }

  public TagRealm setLanguage(String language) {
    this.language = language;
    return this;
  }

  public long getPosts() {
    return posts;
  }

  public TagRealm setPosts(long posts) {
    this.posts = posts;
    return this;
  }

  public boolean isRecommended() {
    return isRecommended;
  }

  public TagRealm setRecommended(boolean recommended) {
    isRecommended = recommended;
    return this;
  }
}