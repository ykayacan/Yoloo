package com.yoloo.android.feature.search;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.util.glide.CropCircleTransformation;

public class UserModel extends EpoxyModelWithHolder<UserModel.UserHolder> {

  @EpoxyAttribute AccountRealm account;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) OnFollowClickListener onFollowClickListener;

  @Override protected UserHolder createNewHolder() {
    return new UserHolder();
  }

  @Override protected int getDefaultLayout() {
    return R.layout.item_search_user;
  }

  @Override public void bind(UserHolder holder) {
    final Context context = holder.ivAvatar.getContext();

    Glide.with(context)
        .load(account.getAvatarUrl())
        .bitmapTransform(CropCircleTransformation.getInstance(context))
        .into(holder.ivAvatar);

    holder.tvUsername.setText(account.getUsername());

    holder.btnFollow.setVisibility(account.isFollowing() ? View.GONE : View.VISIBLE);

    holder.btnFollow.setOnClickListener(v -> {
      Snackbar.make(v, context.getString(R.string.label_search_followed, account.getUsername()),
          Snackbar.LENGTH_SHORT).show();
      holder.btnFollow.setVisibility(View.GONE);
      onFollowClickListener.onFollowClick(v, account.getId(), 1);
    });
    holder.viewGroup.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, account.getId()));
  }

  @Override public void unbind(UserHolder holder) {
    holder.viewGroup.setOnClickListener(null);
    holder.btnFollow.setOnClickListener(null);
  }

  static class UserHolder extends BaseEpoxyHolder {
    @BindView(R.id.layout_item_search) ViewGroup viewGroup;
    @BindView(R.id.iv_item_search_avatar) ImageView ivAvatar;
    @BindView(R.id.tv_item_search_username) TextView tvUsername;
    @BindView(R.id.btn_item_search_follow) Button btnFollow;
  }
}
