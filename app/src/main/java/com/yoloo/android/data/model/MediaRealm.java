package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.MediaDTO;
import com.yoloo.backend.yolooApi.model.Size;
import io.realm.MediaRealmRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.List;
import java.util.Objects;
import org.parceler.Parcel;

@Parcel(implementations = { MediaRealmRealmProxy.class },
    value = Parcel.Serialization.FIELD,
    analyze = { MediaRealm.class })
public class MediaRealm extends RealmObject {

  @PrimaryKey String id;
  @Index String ownerId;
  String mime;
  String thumbSizeUrl;
  String miniSizeUrl;
  String lowSizeUrl;
  String mediumSizeUrl;
  String largeSizeUrl;
  @Ignore String tempPath;

  public MediaRealm() {
    // empty constructor
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

  public String getMiniSizeUrl() {
    return miniSizeUrl;
  }

  public String getLowSizeUrl() {
    return lowSizeUrl;
  }

  public String getMediumSizeUrl() {
    return mediumSizeUrl;
  }

  public String getLargeSizeUrl() {
    return largeSizeUrl;
  }

  public String getTempPath() {
    return tempPath;
  }

  public void setTempPath(String tempPath) {
    this.tempPath = tempPath;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MediaRealm that = (MediaRealm) o;
    return Objects.equals(id, that.id)
        && Objects.equals(ownerId, that.ownerId)
        && Objects.equals(mime, that.mime)
        && Objects.equals(thumbSizeUrl, that.thumbSizeUrl)
        && Objects.equals(miniSizeUrl, that.miniSizeUrl)
        && Objects.equals(lowSizeUrl, that.lowSizeUrl)
        && Objects.equals(mediumSizeUrl, that.mediumSizeUrl)
        && Objects.equals(largeSizeUrl, that.largeSizeUrl);
  }

  @Override public int hashCode() {
    return Objects.hash(id, ownerId, mime, thumbSizeUrl, miniSizeUrl, lowSizeUrl, mediumSizeUrl,
        largeSizeUrl);
  }

  @Override public String toString() {
    return "MediaRealm{"
        + "id='"
        + id
        + '\''
        + ", ownerId='"
        + ownerId
        + '\''
        + ", mime='"
        + mime
        + '\''
        + ", thumbSizeUrl='"
        + thumbSizeUrl
        + '\''
        + ", miniSizeUrl='"
        + miniSizeUrl
        + '\''
        + ", lowSizeUrl='"
        + lowSizeUrl
        + '\''
        + ", mediumSizeUrl='"
        + mediumSizeUrl
        + '\''
        + ", largeSizeUrl='"
        + largeSizeUrl
        + '\''
        + '}';
  }
}
