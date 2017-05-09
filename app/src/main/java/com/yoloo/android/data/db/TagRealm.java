package com.yoloo.android.data.db;

import com.yoloo.backend.yolooApi.model.TagDTO;
import io.realm.RealmObject;
import io.realm.TagRealmRealmProxy;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Objects;
import org.parceler.Parcel;

@Parcel(implementations = {TagRealmRealmProxy.class},
    value = Parcel.Serialization.FIELD,
    analyze = {TagRealm.class})
public class TagRealm extends RealmObject implements Chipable {

  @PrimaryKey String id;
  String name;
  @Index long postCount;
  @Index boolean recommended;
  @Index boolean recent;

  public TagRealm() {
  }

  public TagRealm(TagDTO dto) {
    this.id = dto.getId();
    this.name = dto.getName();
    this.postCount = dto.getPostCount();
  }

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

  public long getPostCount() {
    return postCount;
  }

  public TagRealm setPostCount(long postCount) {
    this.postCount = postCount;
    return this;
  }

  public boolean isRecommended() {
    return recommended;
  }

  public TagRealm setRecommended(boolean recommended) {
    this.recommended = recommended;
    return this;
  }

  public boolean isRecent() {
    return recent;
  }

  public TagRealm setRecent(boolean recent) {
    this.recent = recent;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TagRealm)) return false;
    TagRealm tagRealm = (TagRealm) o;
    return getPostCount() == tagRealm.getPostCount()
        && isRecommended() == tagRealm.isRecommended()
        && isRecent() == tagRealm.isRecent()
        && Objects.equals(getId(), tagRealm.getId())
        && Objects.equals(getName(), tagRealm.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName(), getPostCount(), isRecommended(), isRecent());
  }

  @Override
  public String toString() {
    return "TagRealm{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", postCount="
        + postCount
        + ", recommended="
        + recommended
        + ", recent="
        + recent
        + '}';
  }
}
