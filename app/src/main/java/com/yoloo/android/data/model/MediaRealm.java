package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.MediaDTO;
import com.yoloo.backend.yolooApi.model.Size;

import java.util.List;
import java.util.Objects;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class MediaRealm extends RealmObject {

  @PrimaryKey
  private String id;
  @Index
  private String ownerId;
  private String mime;
  private String thumbSizeUrl;
  private String miniSizeUrl;
  private String lowSizeUrl;
  private String mediumSizeUrl;
  private String largeSizeUrl;

  public MediaRealm() {
  }

  public MediaRealm(MediaDTO dto) {
    id = dto.getId();
    ownerId = dto.getOwnerId();
    mime = dto.getMime();

    List<Size> sizes = dto.getSizes();
    thumbSizeUrl = sizes.get(0).getUrl();
    miniSizeUrl = sizes.get(1).getUrl();
    lowSizeUrl = sizes.get(2).getUrl();
    mediumSizeUrl = sizes.get(3).getUrl();
    largeSizeUrl = sizes.get(4).getUrl();
  }

  public String getId() {
    return id;
  }

  public MediaRealm setId(String id) {
    this.id = id;
    return this;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getMime() {
    return mime;
  }

  public void setMime(String mime) {
    this.mime = mime;
  }

  public String getThumbSizeUrl() {
    return thumbSizeUrl;
  }

  public MediaRealm setThumbSizeUrl(String thumbSizeUrl) {
    this.thumbSizeUrl = thumbSizeUrl;
    return this;
  }

  public String getMiniSizeUrl() {
    return miniSizeUrl;
  }

  public MediaRealm setMiniSizeUrl(String miniSizeUrl) {
    this.miniSizeUrl = miniSizeUrl;
    return this;
  }

  public String getLowSizeUrl() {
    return lowSizeUrl;
  }

  public MediaRealm setLowSizeUrl(String lowSizeUrl) {
    this.lowSizeUrl = lowSizeUrl;
    return this;
  }

  public String getMediumSizeUrl() {
    return mediumSizeUrl;
  }

  public MediaRealm setMediumSizeUrl(String mediumSizeUrl) {
    this.mediumSizeUrl = mediumSizeUrl;
    return this;
  }

  public String getLargeSizeUrl() {
    return largeSizeUrl;
  }

  public MediaRealm setLargeSizeUrl(String largeSizeUrl) {
    this.largeSizeUrl = largeSizeUrl;
    return this;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MediaRealm that = (MediaRealm) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(ownerId, that.ownerId) &&
        Objects.equals(mime, that.mime) &&
        Objects.equals(thumbSizeUrl, that.thumbSizeUrl) &&
        Objects.equals(miniSizeUrl, that.miniSizeUrl) &&
        Objects.equals(lowSizeUrl, that.lowSizeUrl) &&
        Objects.equals(mediumSizeUrl, that.mediumSizeUrl) &&
        Objects.equals(largeSizeUrl, that.largeSizeUrl);
  }

  @Override public int hashCode() {
    return Objects.hash(id, ownerId, mime, thumbSizeUrl, miniSizeUrl, lowSizeUrl, mediumSizeUrl,
        largeSizeUrl);
  }

  @Override public String toString() {
    return "MediaRealm{" +
        "id='" + id + '\'' +
        ", ownerId='" + ownerId + '\'' +
        ", mime='" + mime + '\'' +
        ", thumbSizeUrl='" + thumbSizeUrl + '\'' +
        ", miniSizeUrl='" + miniSizeUrl + '\'' +
        ", lowSizeUrl='" + lowSizeUrl + '\'' +
        ", mediumSizeUrl='" + mediumSizeUrl + '\'' +
        ", largeSizeUrl='" + largeSizeUrl + '\'' +
        '}';
  }
}
