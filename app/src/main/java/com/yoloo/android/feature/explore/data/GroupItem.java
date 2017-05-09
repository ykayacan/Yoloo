package com.yoloo.android.feature.explore.data;

import com.yoloo.android.data.db.GroupRealm;

public class GroupItem implements ExploreItem<GroupRealm> {

  private final GroupRealm group;

  public GroupItem(GroupRealm group) {
    this.group = group;
  }

  @Override
  public GroupRealm getItem() {
    return group;
  }
}
