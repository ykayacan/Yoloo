package com.yoloo.android.feature.chat.createdialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

import java.util.List;

import butterknife.BindView;

class CreateDialogContactAdapter extends SelectableAdapter {

  private final OnItemClickListener<AccountRealm> onItemClickListener;
  private final CropCircleTransformation circleTransformation;

  CreateDialogContactAdapter(Context context,
      OnItemClickListener<AccountRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
    circleTransformation = new CropCircleTransformation(context);

    enableDiffing();
  }

  void addContacts(List<AccountRealm> accounts) {
    for (AccountRealm account : accounts) {
      models.add(new CreateDialogContactAdapter$CreateDialogContactModel_()
          .account(account)
          .adapter(this)
          .onItemClickListener(onItemClickListener)
          .cropCircleTransformation(circleTransformation));
    }

    notifyModelsChanged();
  }

  void clear() {
    models.clear();
  }

  @EpoxyModelClass(layout = R.layout.item_cratedialog_contact)
  public static abstract class CreateDialogContactModel
      extends EpoxyModelWithHolder<CreateDialogContactModel.ContactHolder> {

    @EpoxyAttribute AccountRealm account;
    @EpoxyAttribute(hash = false) CreateDialogContactAdapter adapter;
    @EpoxyAttribute(hash = false) OnItemClickListener<AccountRealm> onItemClickListener;
    @EpoxyAttribute(hash = false) CropCircleTransformation cropCircleTransformation;

    @Override public void bind(CreateDialogContactModel.ContactHolder holder) {
      final Context context = holder.ivAvatar.getContext().getApplicationContext();

      holder.tvUsername.setText(account.getUsername());
      Glide.with(context)
          .load(account.getAvatarUrl())
          .bitmapTransform(cropCircleTransformation)
          .into(holder.ivAvatar);

      final boolean isSelected = adapter.isSelected(this);
      holder.checkmarkView.setVisibility(isSelected ? View.VISIBLE : View.GONE);

      holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, account));

      holder.itemView.setOnLongClickListener(v -> {
        adapter.toggleSelection(this);
        return true;
      });
    }

    public AccountRealm getAccount() {
      return account;
    }

    static class ContactHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_createdialog_contact_avatar) ImageView ivAvatar;
      @BindView(R.id.tv_createdialog_contact_username) TextView tvUsername;
      @BindView(R.id.view_createdialog_contact_checkmark) View checkmarkView;
    }
  }
}
