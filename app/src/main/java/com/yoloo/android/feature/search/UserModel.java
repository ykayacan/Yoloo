package com.yoloo.android.feature.search;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_user)
public abstract class UserModel extends EpoxyModelWithHolder<UserModel.UserHolder> {

  @EpoxyAttribute AccountRealm account;
  @EpoxyAttribute boolean showFollowButton;
  @EpoxyAttribute(DoNotHash) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(DoNotHash) OnUserClickListener onUserClickListener;
  @EpoxyAttribute(DoNotHash) OnFollowClickListener onFollowClickListener;
  @EpoxyAttribute(DoNotHash) CropCircleTransformation cropCircleTransformation;

  @Override
  public void bind(UserHolder holder) {
    final Context context = holder.itemView.getContext();

    Glide
        .with(context)
        .load(account.getAvatarUrl())
        .bitmapTransform(cropCircleTransformation)
        .placeholder(R.drawable.ic_player_72dp)
        .into(holder.ivAvatar);

    holder.tvUsername.setText(account.getUsername());

    holder.btnFollow.setVisibility(showFollowButton ? View.VISIBLE : View.GONE);

    holder.btnFollow.setOnClickListener(v -> {
      //account.setFollowing(!account.isFollowing());

      onFollowClickListener.onFollowClick(v, account, 1);
    });

    if (onProfileClickListener != null) {
      holder.itemView.setOnClickListener(
          v -> onProfileClickListener.onProfileClick(v, account.getId()));
    }

    if (onUserClickListener != null) {
      holder.itemView.setOnClickListener(v -> onUserClickListener.onUserClicked(account));
    }
  }

  @Override
  public void unbind(UserHolder holder) {
    Glide.clear(holder.ivAvatar);
    holder.ivAvatar.setImageDrawable(null);

    holder.itemView.setOnClickListener(null);
    holder.btnFollow.setOnClickListener(null);
  }

  public interface OnUserClickListener {
    void onUserClicked(AccountRealm account);
  }

  static class UserHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_search_avatar) ImageView ivAvatar;
    @BindView(R.id.tv_item_search_username) TextView tvUsername;
    @BindView(R.id.btn_item_search_follow) Button btnFollow;
  }
}
