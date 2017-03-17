package com.yoloo.android.feature.chat.compose;

import android.content.Context;
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

@EpoxyModelClass(layout = R.layout.item_crateconversation_contact)
public abstract class ContactModel extends EpoxyModelWithHolder<ContactModel.ContactHolder> {

  @EpoxyAttribute AccountRealm account;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) CropCircleTransformation cropCircleTransformation;

  @Override public void bind(ContactHolder holder) {
    final Context context = holder.ivAvatar.getContext().getApplicationContext();

    holder.tvUsername.setText(account.getUsername());
    Glide.with(context)
        .load(account.getAvatarUrl())
        .bitmapTransform(cropCircleTransformation)
        .into(holder.ivAvatar);

    holder.itemView.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, account.getId()));
  }

  static class ContactHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_contact_avatar) ImageView ivAvatar;
    @BindView(R.id.tv_contact_username) TextView tvUsername;
  }
}
