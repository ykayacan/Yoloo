package com.yoloo.android.feature.models;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;
import static com.airbnb.epoxy.EpoxyAttribute.Option.NoGetter;

@EpoxyModelClass(layout = R.layout.item_group_grid)
public abstract class GroupGridModel extends EpoxyModelWithHolder<GroupGridModel.GroupGridHolder> {

  @EpoxyAttribute protected String groupImageUrl;
  @EpoxyAttribute protected String groupTitle;
  @EpoxyAttribute({DoNotHash, NoGetter}) protected RequestManager glide;
  @EpoxyAttribute(DoNotHash) protected View.OnClickListener onClickListener;
  @EpoxyAttribute protected float roundCorners = 0.0F;
  @EpoxyAttribute protected String imageQualityModifier = "150";

  @Override
  public void bind(GroupGridHolder holder) {
    super.bind(holder);

    if (roundCorners == 0.0F) {
      glide
          .load(groupImageUrl + "=s" + imageQualityModifier + "-rw")
          .into(holder.ivGroupBackground);
    } else {
      glide
          .load(groupImageUrl + "=s" + imageQualityModifier + "-rw")
          .asBitmap()
          .diskCacheStrategy(DiskCacheStrategy.SOURCE)
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap,
                GlideAnimation<? super Bitmap> glideAnimation) {
              RoundedBitmapDrawable rbd =
                  RoundedBitmapDrawableFactory.create(holder.itemView.getContext().getResources(),
                      bitmap);
              rbd.setCornerRadius(roundCorners);
              holder.ivGroupBackground.setImageDrawable(rbd);
            }
          });
    }

    holder.tvGroupText.setText(groupTitle);
    holder.itemView.setOnClickListener(onClickListener);
  }

  @Override
  public void unbind(GroupGridHolder holder) {
    super.unbind(holder);
    holder.itemView.setOnClickListener(null);
  }

  public static class GroupGridHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_group_bg) ImageView ivGroupBackground;
    @BindView(R.id.tv_group_text) TextView tvGroupText;
  }
}
