package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.db.GroupRealm;
import java.util.List;

public class RecommendedGroupListItem implements FeedItem {
  private final String id;
  private final List<GroupRealm> groups;

  public RecommendedGroupListItem(List<GroupRealm> groups) {
    this.groups = groups;
    this.id = RecommendedGroupListItem.class.getName();
  }

  public String getId() {
    return id;
  }

  public List<GroupRealm> getGroups() {
    return groups;
  }
}
