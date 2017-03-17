package com.yoloo.android.feature.comment;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.ui.widget.VoteView;
import com.yoloo.android.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

@EpoxyModelClass(layout = R.layout.item_comment)
public abstract class CommentModel extends EpoxyModelWithHolder<CommentModel.CommentHolder> {

  private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;

  @EpoxyAttribute CommentRealm comment;
  @EpoxyAttribute boolean isPostOwner;
  @EpoxyAttribute boolean isCommentOwner;
  @EpoxyAttribute boolean postAccepted;
  @EpoxyAttribute int postType;
  @EpoxyAttribute @ColorInt int backgroundColor;
  @EpoxyAttribute(hash = false) OnItemLongClickListener<CommentRealm> onCommentLongClickListener;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) OnVoteClickListener onVoteClickListener;
  @EpoxyAttribute(hash = false) OnMentionClickListener onMentionClickListener;
  @EpoxyAttribute(hash = false) OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
  @EpoxyAttribute(hash = false) CropCircleTransformation circleTransformation;

  @Override public void bind(CommentHolder holder) {
    final Context context = holder.itemView.getContext();

    backgroundColor = backgroundColor == 0 ? DEFAULT_BACKGROUND_COLOR : backgroundColor;
    holder.itemView.setBackgroundColor(backgroundColor);

    Glide.with(context)
        .load(comment.getAvatarUrl())
        .bitmapTransform(circleTransformation)
        .placeholder(R.drawable.ic_player)
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(comment.getUsername());
    holder.tvTime.setTimeStamp(comment.getCreated().getTime() / 1000);
    holder.tvContent.setText(comment.getContent());
    holder.voteView.setVotes(comment.getVoteCount());
    holder.voteView.setVoteDirection(comment.getVoteDir());

    holder.tvAcceptedMark.setVisibility(comment.isAccepted() ? View.VISIBLE : View.GONE);
    holder.tvAccept.setVisibility(isPostOwner
        /*&& !isCommentOwner*/
        && !postAccepted
        && postType != PostRealm.POST_BLOG
        ? View.VISIBLE : View.GONE);

    DrawableHelper.create()
        .withDrawable(holder.tvAccept.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();

    DrawableHelper.create()
        .withDrawable(holder.tvAcceptedMark.getCompoundDrawables()[1])
        .withColor(context, R.color.accepted)
        .tint();

    setupClickListeners(holder);
  }

  private void setupClickListeners(CommentHolder holder) {
    if (isCommentOwner) {
      holder.itemView.setOnLongClickListener(v -> {
        onCommentLongClickListener.onItemLongClick(v, this, comment);
        return true;
      });
    }
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, comment.getOwnerId()));
    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, comment.getOwnerId()));
    holder.tvContent.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.MENTION) {
        onMentionClickListener.onMentionClick(value);
      }
    });

    holder.tvAccept.setOnClickListener(v -> {
      comment.setAccepted(true);
      holder.tvAccept.setVisibility(View.GONE);
      holder.tvAcceptedMark.setVisibility(View.VISIBLE);
      DrawableHelper.create()
          .withDrawable(holder.tvAcceptedMark.getCompoundDrawables()[1])
          .withColor(v.getContext(), R.color.accepted)
          .tint();

      onMarkAsAcceptedClickListener.onMarkAsAccepted(v, comment);
    });
    holder.voteView.setOnVoteEventListener(direction -> {
      comment.setVoteDir(direction);
      onVoteClickListener.onVoteClick(comment.getId(), direction, OnVoteClickListener.Type.COMMENT);
    });
  }

  static class CommentHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_comment_user_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_comment_username) TextView tvUsername;
    @BindView(R.id.tv_comment_time) TimeTextView tvTime;
    @BindView(R.id.tv_comment_accepted) TextView tvAcceptedMark;
    @BindView(R.id.tv_comment_content) LinkableTextView tvContent;
    @BindView(R.id.tv_comment_vote) VoteView voteView;
    @BindView(R.id.tv_mark_as_accepted) TextView tvAccept;
  }
}
