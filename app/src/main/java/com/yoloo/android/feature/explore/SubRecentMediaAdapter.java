package com.yoloo.android.feature.explore;

import com.airbnb.epoxy.EpoxyAdapter;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.feature.explore.model.SubRecentMediaModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class SubRecentMediaAdapter extends EpoxyAdapter {

  private OnItemClickListener<MediaRealm> onItemClickListener;
  private final RequestManager glide;

  public SubRecentMediaAdapter(RequestManager glide) {
    this.glide = glide;
  }

  public void setOnItemClickListener(OnItemClickListener<MediaRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void addItems(List<MediaRealm> medias) {
    for (MediaRealm media : medias) {
      addModel(createModel(media));
    }
  }

  private SubRecentMediaModel_ createModel(MediaRealm media) {
    return new SubRecentMediaModel_()
        .media(media)
        .glide(glide)
        .onItemClickListener(onItemClickListener);
  }
}
