package com.yoloo.android.feature.comment;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.feature.ui.widget.VoteView;
import com.yoloo.android.feature.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.feature.ui.widget.zamanview.ZamanTextView;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.glide.CropCircleTransformation;

public class CommentModel extends EpoxyModelWithHolder<CommentModel.CommentHolder> {

  @EpoxyAttribute CommentRealm comment;

  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;

  @EpoxyAttribute(hash = false) OnVoteClickListener onVoteClickListener;

  @EpoxyAttribute(hash = false) OnMentionClickListener onMentionClickListener;

  @Override protected CommentHolder createNewHolder() {
    return new CommentHolder();
  }

  @Override protected int getDefaultLayout() {
    return R.layout.item_comment;
  }

  @Override public void bind(CommentHolder holder) {
    final Context context = holder.ivUserAvatar.getContext().getApplicationContext();

    Glide.with(context)
        .load(comment.getAvatarUrl())
        .bitmapTransform(CropCircleTransformation.getInstance(context))
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(comment.getUsername());
    holder.tvTime.setTimeStamp(comment.getCreated().getTime() / 1000);
    holder.tvContent.setText(comment.getContent());
    holder.voteView.setVotes(comment.getVotes());
    holder.voteView.setCurrentStatus(comment.getDir());

    if (comment.isAccepted()) {
      holder.tvMarkAsAccepted.setVisibility(View.GONE);
    } else {
      holder.tvMarkAsAccepted.setVisibility(View.VISIBLE);
      DrawableHelper.withContext(context)
          .withDrawable(holder.tvMarkAsAccepted.getCompoundDrawables()[0])
          .withColor(android.R.color.secondary_text_dark)
          .tint();
    }

    setupClickListeners(holder);
  }

  @Override public void unbind(CommentHolder holder) {
    clearClickListeners(holder);
  }

  private void setupClickListeners(CommentHolder holder) {
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, comment.getOwnerId()));
    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, comment.getOwnerId()));
    holder.tvContent.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.MENTION) {
        onMentionClickListener.onMentionClick(value);
      }
    });
    holder.voteView.setOnVoteEventListener(
        direction -> onVoteClickListener.onVoteClick(comment.getId(), direction,
            OnVoteClickListener.VotableType.COMMENT));
  }

  private void clearClickListeners(CommentHolder holder) {
    holder.ivUserAvatar.setOnClickListener(null);
    holder.tvUsername.setOnClickListener(null);
    holder.voteView.setOnVoteEventListener(null);
  }

  static class CommentHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_comment_user_avatar) ImageView ivUserAvatar;

    @BindView(R.id.tv_comment_username) TextView tvUsername;

    @BindView(R.id.tv_comment_time) ZamanTextView tvTime;

    @BindView(R.id.tv_comment_content) LinkableTextView tvContent;

    @BindView(R.id.tv_comment_vote) VoteView voteView;

    @BindView(R.id.tv_mark_as_accepted) TextView tvMarkAsAccepted;
  }
}