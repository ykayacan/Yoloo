package com.yoloo.android.feature.explore;

import com.yoloo.android.data.model.MediaRealm;
import java.util.List;

public class RecentMediaItem implements ExploreItem<List<MediaRealm>> {

  private final List<MediaRealm> medias;

  public RecentMediaItem(List<MediaRealm> medias) {
    this.medias = medias;
  }

  @Override
  public List<MediaRealm> getItem() {
    return medias;
  }
}
