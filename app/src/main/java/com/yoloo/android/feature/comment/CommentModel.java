package com.yoloo.android.feature.comment;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.feature.feed.common.annotation.PostType;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.feature.ui.widget.VoteView;
import com.yoloo.android.feature.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.feature.ui.widget.zamanview.TimeTextView;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.glide.CropCircleTransformation;

public class CommentModel extends EpoxyModelWithHolder<CommentModel.CommentHolder> {

  @EpoxyAttribute CommentRealm comment;
  @EpoxyAttribute boolean isPostOwner;
  @EpoxyAttribute boolean isCommentOwner;
  @EpoxyAttribute boolean postAccepted;
  @EpoxyAttribute int postType;
  @EpoxyAttribute(hash = false) OnCommentLongClickListener onCommentLongClickListener;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) OnVoteClickListener onVoteClickListener;
  @EpoxyAttribute(hash = false) OnMentionClickListener onMentionClickListener;
  @EpoxyAttribute(hash = false) OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;

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

    holder.tvAcceptedMark.setVisibility(comment.isAccepted() ? View.VISIBLE : View.GONE);
    holder.tvAccept.setVisibility(isPostOwner
        /*&& !isCommentOwner*/
        && !postAccepted
        && postType != PostType.TYPE_BLOG
        ? View.VISIBLE : View.GONE);

    DrawableHelper.withContext(context)
        .withDrawable(holder.tvAccept.getCompoundDrawables()[0])
        .withColor(android.R.color.secondary_text_dark)
        .tint();

    DrawableHelper.withContext(context)
        .withDrawable(holder.tvAcceptedMark.getCompoundDrawables()[1])
        .withColor(R.color.accepted)
        .tint();

    setupClickListeners(holder);
  }

  private void setupClickListeners(CommentHolder holder) {
    if (isCommentOwner) {
      holder.root.setOnLongClickListener(v -> {
        onCommentLongClickListener.onCommentLongClick(v, this, comment);
        return true;
      });
      holder.tvContent.setOnLongClickListener(v -> {
        onCommentLongClickListener.onCommentLongClick(v, this, comment);
        return true;
      });
    }
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, comment.getOwnerId()));
    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, comment.getOwnerId()));
    holder.tvContent.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.MENTION) {
        onMentionClickListener.onMentionClick(value);
      }
    });

    holder.tvAccept.setOnClickListener(v -> {
      comment.setAccepted(true);
      holder.tvAccept.setVisibility(View.GONE);
      holder.tvAcceptedMark.setVisibility(View.VISIBLE);
      DrawableHelper.withContext(v.getContext())
          .withDrawable(holder.tvAcceptedMark.getCompoundDrawables()[1])
          .withColor(R.color.accepted)
          .tint();

      onMarkAsAcceptedClickListener.onMarkAsAccepted(v, comment);
    });
    holder.voteView.setOnVoteEventListener(direction -> {
      comment.setDir(direction);
      onVoteClickListener.onVoteClick(comment.getId(), direction, OnVoteClickListener.Type.COMMENT);
    });
  }

  static class CommentHolder extends BaseEpoxyHolder {
    @BindView(R.id.root_view) ViewGroup root;
    @BindView(R.id.iv_comment_user_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_comment_username) TextView tvUsername;
    @BindView(R.id.tv_comment_time) TimeTextView tvTime;
    @BindView(R.id.tv_comment_accepted) TextView tvAcceptedMark;
    @BindView(R.id.tv_comment_content) LinkableTextView tvContent;
    @BindView(R.id.tv_comment_vote) VoteView voteView;
    @BindView(R.id.tv_mark_as_accepted) TextView tvAccept;
  }
}