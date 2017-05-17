package com.yoloo.android.feature.models;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_new_user_welcome)
public abstract class NewUserWelcome
    extends EpoxyModelWithHolder<NewUserWelcome.NewUserWelcomeHolder> {

  @EpoxyAttribute AccountRealm account;
  @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;
  @EpoxyAttribute(DoNotHash) Transformation<Bitmap> transformation;
  @EpoxyAttribute(DoNotHash) RequestManager glide;

  @Override public void bind(NewUserWelcomeHolder holder) {
    super.bind(holder);
    holder.itemView.setOnClickListener(onClickListener);

    glide.load(account.getAvatarUrl()).bitmapTransform(transformation).into(holder.ivAvatar);
    holder.tvUsername.setText(account.getUsername());
  }

  static class NewUserWelcomeHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) ImageView ivAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
  }
}
