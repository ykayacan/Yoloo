package com.yoloo.android.feature.models;

import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.widget.AvatarView;
import com.yoloo.android.util.glide.AvatarTarget;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_new_user_welcome)
public abstract class NewUserWelcomeModel
    extends EpoxyModelWithHolder<NewUserWelcomeModel.NewUserWelcomeHolder> {

  @EpoxyAttribute AccountRealm account;
  @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;
  @EpoxyAttribute(DoNotHash) RequestManager glide;

  @Override public void bind(NewUserWelcomeHolder holder) {
    super.bind(holder);
    holder.itemView.setOnClickListener(onClickListener);

    glide.load(account.getAvatarUrl()).into(new AvatarTarget(holder.ivAvatar));
    holder.tvUsername.setText(account.getUsername());
  }

  @Override public void unbind(NewUserWelcomeHolder holder) {
    super.unbind(holder);
    holder.itemView.setOnClickListener(null);

    Glide.clear(holder.ivAvatar);
    holder.ivAvatar.setAvatar(null);
  }

  static class NewUserWelcomeHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) AvatarView ivAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
  }
}
