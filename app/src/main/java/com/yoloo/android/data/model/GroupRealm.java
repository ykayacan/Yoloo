package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.TravelerGroup;
import io.realm.GroupRealmRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import org.parceler.Parcel;

@Parcel(implementations = {GroupRealmRealmProxy.class},
    value = Parcel.Serialization.FIELD,
    analyze = {GroupRealm.class})
public class GroupRealm extends RealmObject implements Chipable {

  @PrimaryKey String id;

  @Index String name;

  String backgroundUrl;

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
    backgroundUrl = group.getImageUrl();
    postCount = group.getPostCount();
    subscriberCount = group.getSubscriberCount();
    rank = group.getRank();
    subscribed = group.getSubscribed();
  }

  public String getId() {
    return id;
  }

  public GroupRealm setId(String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public GroupRealm setName(String name) {
    this.name = name;
    return this;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public GroupRealm setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
    return this;
  }

  public long getPostCount() {
    return postCount;
  }

  public GroupRealm setPostCount(long postCount) {
    this.postCount = postCount;
    return this;
  }

  public double getRank() {
    return rank;
  }

  public GroupRealm setRank(double rank) {
    this.rank = rank;
    return this;
  }

  public long getSubscriberCount() {
    return subscriberCount;
  }

  public void setSubscriberCount(long subscriberCount) {
    this.subscriberCount = subscriberCount;
  }

  public boolean isSubscribed() {
    return subscribed;
  }

  public void setSubscribed(boolean subscribed) {
    this.subscribed = subscribed;
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
        + ", backgroundUrl='"
        + backgroundUrl
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GroupRealm that = (GroupRealm) o;

    if (postCount != that.postCount) return false;
    if (subscriberCount != that.subscriberCount) return false;
    if (Double.compare(that.rank, rank) != 0) return false;
    if (subscribed != that.subscribed) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    return backgroundUrl != null
        ? backgroundUrl.equals(that.backgroundUrl)
        : that.backgroundUrl == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (backgroundUrl != null ? backgroundUrl.hashCode() : 0);
    result = 31 * result + (int) (postCount ^ (postCount >>> 32));
    result = 31 * result + (int) (subscriberCount ^ (subscriberCount >>> 32));
    temp = Double.doubleToLongBits(rank);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (subscribed ? 1 : 0);
    return result;
  }
}
