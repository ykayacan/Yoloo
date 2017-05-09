package com.yoloo.android.feature.explore.data;

import com.yoloo.android.data.db.PostRealm;
import java.util.List;

public class RecentMediaListItem implements ExploreItem<List<PostRealm>> {

  private final List<PostRealm> posts;

  public RecentMediaListItem(List<PostRealm> posts) {
    this.posts = posts;
  }

  @Override
  public List<PostRealm> getItem() {
    return posts;
  }
}
