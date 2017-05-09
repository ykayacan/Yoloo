package com.yoloo.android.feature.models.newusers;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.widget.Button;
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
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_new_user)
public abstract class NewUserModel
    extends EpoxyModelWithHolder<NewUserModel.NewcomersContactHolder> {

  @EpoxyAttribute AccountRealm account;
  @EpoxyAttribute(DoNotHash) RequestManager glide;
  @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;
  @EpoxyAttribute(DoNotHash) OnFollowClickListener onFollowClickListener;

  @Override
  public void bind(NewcomersContactHolder holder) {
    final Context context = holder.itemView.getContext();

    glide
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

    holder.btnFollow.setOnClickListener(v -> onFollowClickListener.onFollowClick(v, account, 1));
    holder.itemView.setOnClickListener(onClickListener);
  }

  @Override
  public void unbind(NewcomersContactHolder holder) {
    super.unbind(holder);
    holder.itemView.setOnClickListener(null);
    holder.btnFollow.setOnClickListener(null);
  }

  static class NewcomersContactHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_newcomers_avatar) ImageView ivAvatar;
    @BindView(R.id.tv_newcomers_username) TextView tvUsername;
    @BindView(R.id.btn_newcomers_follow) Button btnFollow;
  }
}
