package com.yoloo.android.feature.explore.data;

import android.support.annotation.NonNull;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.util.Objects;
import javax.annotation.Nonnull;

import static com.yoloo.android.util.Preconditions.checkNotNull;

public class GroupItem implements FeedItem<GroupRealm> {

  private final GroupRealm group;

  public GroupItem(@Nonnull GroupRealm group) {
    checkNotNull(group, "group cannot be null");
    this.group = group;
  }

  @Nonnull @Override public String getId() {
    return group.getId();
  }

  @NonNull @Override public GroupRealm getItem() {
    return group;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GroupItem groupItem = (GroupItem) o;
    return Objects.equal(group, groupItem.group);
  }

  @Override public int hashCode() {
    return Objects.hashCode(group);
  }

  @Override public String toString() {
    return "GroupItem{" +
        "group=" + group +
        '}';
  }
}
