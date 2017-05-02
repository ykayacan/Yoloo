package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.TravelerGroup;
import io.realm.GroupRealmRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Objects;
import org.parceler.Parcel;

@Parcel(implementations = {GroupRealmRealmProxy.class},
    value = Parcel.Serialization.FIELD,
    analyze = {GroupRealm.class})
public class GroupRealm extends RealmObject implements Chipable {

  @PrimaryKey String id;
  @Index String name;
  String imageWithIconUrl;
  String imageWithoutIconUrl;
  long postCount;
  long subscriberCount;
  double rank;
  boolean subscribed;

  public GroupRealm() {
    // empty constructor
  }

  public GroupRealm(TravelerGroup group) {
    id = group.getId();
    name = group.getName();
    imageWithIconUrl = group.getImageWithIconUrl();
    imageWithoutIconUrl = group.getImageWithoutIconUrl();
    postCount = group.getPostCount();
    subscriberCount = group.getSubscriberCount();
    rank = group.getRank();
    subscribed = group.getSubscribed();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getImageWithIconUrl() {
    return imageWithIconUrl;
  }

  public String getImageWithoutIconUrl() {
    return imageWithoutIconUrl;
  }

  public long getPostCount() {
    return postCount;
  }

  public double getRank() {
    return rank;
  }

  public long getSubscriberCount() {
    return subscriberCount;
  }

  public boolean isSubscribed() {
    return subscribed;
  }

  public void setSubscribed(boolean subscribed) {
    this.subscribed = subscribed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GroupRealm)) return false;
    GroupRealm that = (GroupRealm) o;
    return getPostCount() == that.getPostCount()
        && getSubscriberCount() == that.getSubscriberCount()
        && Double.compare(that.getRank(), getRank()) == 0
        && isSubscribed() == that.isSubscribed()
        && Objects.equals(getId(), that.getId())
        && Objects.equals(getName(), that.getName())
        && Objects.equals(getImageWithIconUrl(), that.getImageWithIconUrl())
        && Objects.equals(imageWithoutIconUrl, that.imageWithoutIconUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName(), getImageWithIconUrl(), imageWithoutIconUrl,
        getPostCount(), getSubscriberCount(), getRank(), isSubscribed());
  }

  @Override
  public String toString() {
    return "GroupRealm{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", imageWithIconUrl='"
        + imageWithIconUrl
        + '\''
        + ", imageWithoutIconUrl='"
        + imageWithoutIconUrl
        + '\''
        + ", postCount="
        + postCount
        + ", subscriberCount="
        + subscriberCount
        + ", rank="
        + rank
        + ", subscribed="
        + subscribed
        + '}';
  }
}
