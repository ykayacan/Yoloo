package com.yoloo.android.feature.feed.component.newcomers;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;

@EpoxyModelClass(layout = R.layout.item_feed_newcomers_contact)
abstract class NewcomersContactModel
    extends EpoxyModelWithHolder<NewcomersContactModel.NewcomersContactHolder> {

  @EpoxyAttribute AccountRealm account;
  @EpoxyAttribute(hash = false) OnItemClickListener<AccountRealm> onItemClickListener;
  @EpoxyAttribute(hash = false) OnFollowClickListener onFollowClickListener;

  @Override
  public void bind(NewcomersContactHolder holder) {
    final Context context = holder.itemView.getContext();

    Glide
        .with(context)
        .load(account.getAvatarUrl()/* + "=s80-rw"*/)
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .into(new SimpleTarget<Bitmap>() {
          @Override
          public void onResourceReady(Bitmap resource,
              GlideAnimation<? super Bitmap> glideAnimation) {
            RoundedBitmapDrawable rbd =
                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
            rbd.setCornerRadius(6f);
            holder.ivAvatar.setImageDrawable(rbd);
          }
        });

    holder.tvUsername.setText(account.getUsername());

    holder.btnFollow.setOnClickListener(
        v -> onFollowClickListener.onFollowClick(v, this, account, 1));
    holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, account));
  }

  static class NewcomersContactHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_newcomers_avatar) ImageView ivAvatar;
    @BindView(R.id.tv_newcomers_username) TextView tvUsername;
    @BindView(R.id.btn_newcomers_follow) Button btnFollow;
  }
}
