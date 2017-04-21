package com.yoloo.android.feature.explore.model;

import android.widget.ImageView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;

@EpoxyModelClass(layout = R.layout.item_sub_recent_media)
public abstract class SubRecentMediaModel extends EpoxyModel<ImageView> {

  @EpoxyAttribute MediaRealm media;
  @EpoxyAttribute(hash = false) RequestManager glide;
  @EpoxyAttribute(hash = false) OnItemClickListener<MediaRealm> onItemClickListener;

  @Override
  public void bind(ImageView view) {
    super.bind(view);
    glide.load(media.getMediumSizeUrl()).into(view);

    view.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, media));
  }

  @Override
  public void unbind(ImageView view) {
    super.unbind(view);
    view.setOnClickListener(null);
  }
}
