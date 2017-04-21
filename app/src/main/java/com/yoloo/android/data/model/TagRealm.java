package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.TagDTO;
import io.realm.RealmObject;
import io.realm.TagRealmRealmProxy;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import org.parceler.Parcel;

@Parcel(implementations = { TagRealmRealmProxy.class },
        value = Parcel.Serialization.FIELD,
        analyze = { TagRealm.class })
public class TagRealm extends RealmObject implements Chipable {

  @PrimaryKey String id;
  String name;
  @Index long postCount;
  @Index boolean isRecommended;
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
    return isRecommended;
  }

  public TagRealm setRecommended(boolean recommended) {
    isRecommended = recommended;
    return this;
  }

  public boolean isRecent() {
    return recent;
  }

  public TagRealm setRecent(boolean recent) {
    this.recent = recent;
    return this;
  }

  @Override public String toString() {
    return "TagRealm{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", postCount=" + postCount +
        ", isRecommended=" + isRecommended +
        ", recent=" + recent +
        '}';
  }
}
