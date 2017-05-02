package com.yoloo.android.feature.models.recentmedias;

import android.view.View;
import android.widget.ImageView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_recent_media)
public abstract class RecentMediaModel extends EpoxyModel<ImageView> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute(DoNotHash) RequestManager glide;
  @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;

  @Override
  public void bind(ImageView view) {
    super.bind(view);
    glide.load(post.getMedias().get(0).getMediumSizeUrl()).into(view);

    view.setOnClickListener(onClickListener);
  }

  @Override
  public void unbind(ImageView view) {
    super.unbind(view);
    view.setOnClickListener(null);
  }
}
