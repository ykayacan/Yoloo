package com.yoloo.android.data.model;

import java.util.Objects;
import org.parceler.Parcel;

@Parcel
public class TravelerType {

  protected String id;
  protected String name;
  protected String imageUrl;

  public TravelerType(com.yoloo.backend.yolooApi.model.TravelerType type) {
    this.id = type.getId();
    this.name = type.getName();
    this.imageUrl = type.getImageUrl();
  }

  public TravelerType() {
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TravelerType)) return false;
    TravelerType type = (TravelerType) o;
    return Objects.equals(getId(), type.getId())
        && Objects.equals(getName(), type.getName())
        && Objects.equals(getImageUrl(), type.getImageUrl());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName(), getImageUrl());
  }

  @Override
  public String toString() {
    return "TravelerType{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", imageUrl='"
        + imageUrl
        + '\''
        + '}';
  }
}
