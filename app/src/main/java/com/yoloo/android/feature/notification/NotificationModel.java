package com.yoloo.android.feature.notification;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.model.PayloadMap;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.ui.widget.timeview.TimeManager;
import com.yoloo.android.util.HtmlUtil;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_notification)
abstract class NotificationModel
    extends EpoxyModelWithHolder<NotificationModel.NotificationHolder> {

  @EpoxyAttribute NotificationRealm notification;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) CropCircleTransformation cropCircleTransformation;

  @Override
  public void bind(NotificationHolder holder) {
    final Context context = holder.itemView.getContext();

    switch (notification.getAction()) {
      case NotificationRealm.GAME:
        holder.ivUserAvatar.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_bounty_3));
        break;
      case NotificationRealm.ACCEPT:
        holder.ivUserAvatar.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_check_green_48dp));
        break;
      default:
        Glide
            .with(context)
            .load(notification.getSenderAvatarUrl())
            .bitmapTransform(cropCircleTransformation)
            .into(holder.ivUserAvatar);
        break;
    }

    holder.ibFollow.setVisibility(
        notification.getAction().equals(NotificationRealm.FOLLOW) ? View.VISIBLE : View.GONE);

    final Resources res = context.getResources();

    final String time =
        TimeManager.getInstance().calculateTime(notification.getCreated().getTime() / 1000);

    holder.tvContent.setText(HtmlUtil.fromHtml(
        getActionString(res) + " <font color='" + ContextCompat.getColor(context,
            R.color.editor_icon) + "'>" + time + "</font>"));

    setupClickListeners(holder);
  }

  @Override
  public void unbind(NotificationHolder holder) {
    Glide.clear(holder.ivUserAvatar);

    holder.ivUserAvatar.setImageDrawable(null);
  }

  @NonNull
  private String getActionString(Resources res) {
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
      case NotificationRealm.GAME:
        List<PayloadMap> payloads = notification.getPayload();
        text = res.getString(R.string.label_notification_game_together, payloads.get(0).getValue(),
            payloads.get(1).getValue());
        break;
      default:
        text = "";
        break;
    }
    return text;
  }

  private void setupClickListeners(NotificationHolder holder) {
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, notification.getSenderId()));

    holder.tvContent.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.MENTION) {
        onProfileClickListener.onProfileClick(null, notification.getSenderId());
      }
    });

    holder.ibFollow.setOnClickListener(
        v -> Snackbar.make(v, "User followed.", Snackbar.LENGTH_SHORT).show());
  }

  static class NotificationHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_not_item_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_not_item_content) LinkableTextView tvContent;
    @BindView(R.id.ib_not_follow) ImageButton ibFollow;
  }
}
