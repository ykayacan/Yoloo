package com.yoloo.android.feature.profile.photolist;

import android.view.View;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.ui.widget.SquareImageView;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_photo_grid)
public abstract class MediaModel extends EpoxyModel<SquareImageView> {

  @EpoxyAttribute String mediaUrl;
  @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;
  @EpoxyAttribute(DoNotHash) RequestManager glide;

  @Override
  public void bind(SquareImageView view) {
    super.bind(view);
    glide.load(mediaUrl).into(view);
    view.setOnClickListener(onClickListener);
  }

  @Override
  public void unbind(SquareImageView view) {
    super.unbind(view);
    Glide.clear(view);
    view.setImageDrawable(null);
    view.setOnClickListener(null);
  }
}
