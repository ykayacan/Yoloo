package com.yoloo.android.data.model;

import android.content.res.Resources;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.backend.yolooApi.model.TravelerGroup;
import io.realm.GroupRealmRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import org.parceler.Parcel;

@Parcel(implementations = {GroupRealmRealmProxy.class},
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
    return getLocalizedGroupName(name);
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
        && (getId() != null ? getId().equals(that.getId()) : that.getId() == null)
        && (getName() != null ? getName().equals(that.getName()) : that.getName() == null)
        && (getImageWithIconUrl() != null
        ? getImageWithIconUrl().equals(that.getImageWithIconUrl())
        : that.getImageWithIconUrl() == null)
        && (getImageWithoutIconUrl() != null ? getImageWithoutIconUrl().equals(
        that.getImageWithoutIconUrl()) : that.getImageWithoutIconUrl() == null);
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + (getName() != null ? getName().hashCode() : 0);
    result = 31 * result + (getImageWithIconUrl() != null ? getImageWithIconUrl().hashCode() : 0);
    result =
        31 * result + (getImageWithoutIconUrl() != null ? getImageWithoutIconUrl().hashCode() : 0);
    result = 31 * result + (int) (getPostCount() ^ (getPostCount() >>> 32));
    result = 31 * result + (int) (getSubscriberCount() ^ (getSubscriberCount() >>> 32));
    temp = Double.doubleToLongBits(getRank());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (isSubscribed() ? 1 : 0);
    return result;
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

  private String getLocalizedGroupName(String groupName) {
    Resources res = YolooApp.getAppContext().getResources();

    switch (groupName) {
      case "Activities":
        return res.getString(R.string.group_activities);
      case "Adventure":
        return res.getString(R.string.group_adventure);
      case "Camping":
        return res.getString(R.string.group_camping);
      case "Culture":
        return res.getString(R.string.group_culture);
      case "Events":
        return res.getString(R.string.group_events);
      case "Food & Drink":
        return res.getString(R.string.group_food_drink);
      case "Nightlife":
        return res.getString(R.string.group_nightlife);
      case "Solo Travel":
        return res.getString(R.string.group_solo_travel);
      case "Study Abroad":
        return res.getString(R.string.group_study_abroad);
      case "Tours":
        return res.getString(R.string.group_tours);
      default:
        return null;
    }
  }
}
