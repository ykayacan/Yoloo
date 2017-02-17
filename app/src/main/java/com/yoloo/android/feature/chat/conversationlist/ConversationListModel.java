package com.yoloo.android.feature.chat.conversationlist;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_conversationlist)
abstract class ConversationListModel
    extends EpoxyModelWithHolder<ConversationListModel.ConversationHolder> {

  @EpoxyAttribute Chat chat;
  @EpoxyAttribute(hash = false) ConversationListAdapter adapter;
  @EpoxyAttribute(hash = false) OnItemClickListener<Chat> itemClickListener;
  @EpoxyAttribute(hash = false) OnItemLongClickListener<Chat> onItemLongClickListener;
  @EpoxyAttribute(hash = false) CropCircleTransformation cropCircleTransformation;

  @Override public void bind(ConversationHolder holder, List<Object> payloads) {
    if (payloads.isEmpty()) {
      super.bind(holder, payloads);
    } else {
      if (payloads.get(0) instanceof Chat) {
        Chat payload = (Chat) payloads.get(0);

        if (!payload.getCoverImageUrl().equals(chat.getCoverImageUrl())) {
          final Context context = holder.ivAvatar.getContext().getApplicationContext();

          Glide.with(context)
              .load(payload.getCoverImageUrl())
              .bitmapTransform(cropCircleTransformation)
              .into(holder.ivAvatar);
        }

        if (!payload.getTitle().equals(chat.getTitle())) {
          holder.tvTitle.setText(payload.getTitle());
        }

        if (!payload.getLastMessage().equals(chat.getLastMessage())) {
          holder.tvLastMessage.setText(payload.getLastMessage());
        }

        if (!payload.getMissed().equals(chat.getMissed())) {

        }

        if (payload.getTimestampUpdated() != chat.getTimestampUpdated()) {
          holder.tvTime.setTimeStamp(payload.getTimestampUpdated());
        }
      }
    }
  }

  @Override public void bind(ConversationHolder holder) {
    holder.tvTitle.setText(chat.getTitle());
    holder.tvLastMessage.setText(chat.getLastMessage());
    holder.tvTime.setTimeStamp(chat.getTimestampUpdated() / 1000);
    holder.tvUnreadCount.setText(getUnreadCount());
    holder.tvUnreadCount.setVisibility(chat.getMissed() == 0 ? View.GONE : View.VISIBLE);

    final Context context = holder.ivAvatar.getContext().getApplicationContext();

    Glide.with(context)
        .load(chat.getCoverImageUrl())
        .bitmapTransform(cropCircleTransformation)
        .into(holder.ivAvatar);

    holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(v, this, chat));

    holder.itemView.setOnLongClickListener(v -> {
      if (adapter.toggleSelection(this)) {
        onItemLongClickListener.onItemLongClick(v, this, chat);
        return true;
      } else {
        return false;
      }
    });
  }

  public Chat getChat() {
    return chat;
  }

  private String getUnreadCount() {
    return chat.getMissed() > 9 ? "9+" : chat.getMissed().toString();
  }

  static class ConversationHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_chat_title) TextView tvTitle;
    @BindView(R.id.tv_chat_last_message) TextView tvLastMessage;
    @BindView(R.id.tv_chat_time) TimeTextView tvTime;
    @BindView(R.id.iv_chat_avatar) ImageView ivAvatar;
    @BindView(R.id.tv_chat_unread_count) TextView tvUnreadCount;
  }
}
