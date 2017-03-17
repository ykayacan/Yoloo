package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.CategoryDTO;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Objects;

public class CategoryRealm extends RealmObject implements Chipable {

  @PrimaryKey
  private String id;
  @Index
  private String name;
  private String backgroundUrl;
  private long postCount;
  private double rank;

  public CategoryRealm() {
  }

  public CategoryRealm(CategoryDTO dto) {
    id = dto.getId();
    name = dto.getName();
    backgroundUrl = dto.getImageUrl();
    postCount = dto.getPostCount();
    rank = dto.getRank().doubleValue();
  }

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

  public long getPostCount() {
    return postCount;
  }

  public CategoryRealm setPostCount(long postCount) {
    this.postCount = postCount;
    return this;
  }

  public double getRank() {
    return rank;
  }

  public CategoryRealm setRank(double rank) {
    this.rank = rank;
    return this;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CategoryRealm that = (CategoryRealm) o;
    return postCount == that.postCount &&
        Double.compare(that.rank, rank) == 0 &&
        Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(backgroundUrl, that.backgroundUrl);
  }

  @Override public int hashCode() {
    return Objects.hash(id, name, backgroundUrl, postCount, rank);
  }

  @Override public String toString() {
    return "CategoryRealm{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", backgroundUrl='" + backgroundUrl + '\'' +
        ", postCount=" + postCount +
        ", rank=" + rank +
        '}';
  }
}
