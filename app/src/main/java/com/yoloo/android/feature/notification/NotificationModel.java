package com.yoloo.android.feature.notification;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.feature.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.feature.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.feature.ui.widget.zamanview.ZamanManager;
import com.yoloo.android.util.glide.CropCircleTransformation;
import timber.log.Timber;

public class NotificationModel extends EpoxyModelWithHolder<NotificationModel.NotificationHolder> {

  @EpoxyAttribute NotificationRealm notification;

  @Override protected NotificationHolder createNewHolder() {
    return new NotificationHolder();
  }

  @Override protected int getDefaultLayout() {
    return R.layout.item_notification;
  }

  @Override public void bind(NotificationHolder holder) {
    final Context context = holder.ivUserAvatar.getContext();

    Glide.with(context)
        .load(notification.getSenderAvatarUrl())
        .bitmapTransform(CropCircleTransformation.getInstance(context))
        .into(holder.ivUserAvatar);

    holder.ibFollow.setVisibility(
        notification.getAction().equals(NotificationRealm.FOLLOW) ? View.VISIBLE : View.GONE);

    final Resources res = context.getResources();

    final String time = ZamanManager.getInstance(notification.getCreated().getTime()).getTime();

    String text = getActionString(res);
    holder.tvContent.setText(Html.fromHtml(text
        + " <font color='"
        + ContextCompat.getColor(context, R.color.editor_icon)
        + "'>"
        + time
        + "</font>"));

    setupClickListeners(holder);
  }

  @NonNull private String getActionString(Resources res) {
    String text;
    switch (notification.getAction()) {
      case NotificationRealm.FOLLOW:
        text = res.getString(R.string.label_notification_follow, notification.getSenderUsername());
        break;
      case NotificationRealm.MENTION:
        text = res.getString(R.string.label_notification_mention, notification.getSenderUsername(),
            notification.getMessage());
        break;
      case NotificationRealm.COMMENT:
        text = res.getString(R.string.label_notification_comment, notification.getSenderUsername());
        break;
      case NotificationRealm.ACCEPT:
        text = res.getString(R.string.label_notification_accept);
        break;
      default:
        text = "";
        break;
    }
    return text;
  }

  private void setupClickListeners(NotificationHolder holder) {
    holder.ivUserAvatar.setOnClickListener(v -> {
    });

    holder.tvContent.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.MENTION) {
        Timber.d("TRUE: %s", value);
      }
    });

    holder.ibFollow.setOnClickListener(v -> {
      Snackbar.make(v, "User followed.", Snackbar.LENGTH_SHORT).show();
    });
  }

  static class NotificationHolder extends BaseEpoxyHolder {
    @BindView(R.id.layout_notification) ViewGroup viewGroup;

    @BindView(R.id.iv_not_item_avatar) ImageView ivUserAvatar;

    @BindView(R.id.tv_not_item_content) LinkableTextView tvContent;

    @BindView(R.id.ib_not_follow) ImageButton ibFollow;

    @BindView(R.id.iv_not_photo) ImageView ivPhoto;
  }
}
