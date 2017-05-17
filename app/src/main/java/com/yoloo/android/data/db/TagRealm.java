package com.yoloo.android.data.db;

import com.yoloo.backend.yolooApi.model.TagDTO;
import io.realm.RealmObject;
import io.realm.TagRealmRealmProxy;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
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

  public TagRealm(String tagName) {
    this.id = tagName;
    this.name = tagName;
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

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TagRealm)) return false;

    TagRealm tagRealm = (TagRealm) o;

    if (getPostCount() != tagRealm.getPostCount()) return false;
    if (isRecommended() != tagRealm.isRecommended()) return false;
    if (isRecent() != tagRealm.isRecent()) return false;
    if (!getId().equals(tagRealm.getId())) return false;
    return getName().equals(tagRealm.getName());
  }

  @Override public int hashCode() {
    int result = getId().hashCode();
    result = 31 * result + getName().hashCode();
    result = 31 * result + (int) (getPostCount() ^ (getPostCount() >>> 32));
    result = 31 * result + (isRecommended() ? 1 : 0);
    result = 31 * result + (isRecent() ? 1 : 0);
    return result;
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
