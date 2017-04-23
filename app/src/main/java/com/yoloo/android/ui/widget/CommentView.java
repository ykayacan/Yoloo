package com.yoloo.android.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.airbnb.epoxy.EpoxyModel;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.DrawableHelper;
import java.util.Date;

public class CommentView extends ConstraintLayout {

  @BindView(R.id.iv_comment_user_avatar) ImageView ivUserAvatar;
  @BindView(R.id.tv_comment_username) TextView tvUsername;
  @BindView(R.id.tv_comment_time) TimeTextView tvTime;
  @BindView(R.id.tv_comment_accepted_indicator) TextView tvAcceptedIndicator;
  @BindView(R.id.tv_comment_content) LinkableTextView tvContent;
  @BindView(R.id.tv_mark_as_accepted) TextView tvMarkAsAccepted;
  @BindView(R.id.tv_comment_vote) VoteView voteView;

  private OnCommentClickListener onCommentClickListener;
  private EpoxyModel<?> model;
  private CommentRealm comment;

  public CommentView(Context context, AttributeSet attrs) {
    super(context, attrs);
    inflate(getContext(), R.layout.layout_commentview, this);
    ButterKnife.bind(this);

    DrawableHelper
        .create()
        .withDrawable(tvMarkAsAccepted.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();

    DrawableHelper
        .create()
        .withDrawable(tvAcceptedIndicator.getCompoundDrawables()[1])
        .withColor(context, R.color.accepted)
        .tint();
  }

  public void setOnCommentClickListener(OnCommentClickListener onCommentClickListener,
      EpoxyModel<?> model, CommentRealm comment) {
    this.onCommentClickListener = onCommentClickListener;
    this.model = model;
    this.comment = comment;

    tvContent.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.MENTION) {
        onCommentClickListener.onCommentMentionClick(model, value);
      }
    });

    voteView.setOnVoteEventListener(direction -> {
      comment.setVoteDir(direction);
      onCommentClickListener.onCommentVoteClick(model, comment.getId(), direction);
    });

    tvMarkAsAccepted.setOnClickListener(v -> {
      comment.setAccepted(true);
      tvMarkAsAccepted.setVisibility(View.GONE);
      tvAcceptedIndicator.setVisibility(View.VISIBLE);
      DrawableHelper
          .create()
          .withDrawable(tvAcceptedIndicator.getCompoundDrawables()[1])
          .withColor(v.getContext(), R.color.accepted)
          .tint();

      onCommentClickListener.onMarkAsAccepted(model, comment);
    });
  }

  public void setUserAvatar(@NonNull RequestManager glide,
      @NonNull Transformation<Bitmap> transformation, @NonNull String avatarUrl) {
    glide
        .load(avatarUrl)
        .bitmapTransform(transformation)
        .placeholder(R.drawable.ic_player_72dp)
        .into(ivUserAvatar);
  }

  public void setUsername(@NonNull String username) {
    tvUsername.setText(username);
  }

  public void setTime(@NonNull Date date) {
    tvTime.setTimeStamp(date.getTime() / 1000);
  }

  public void setContent(@NonNull String content) {
    tvContent.setText(content);
  }

  public void setVoteCount(long voteCount) {
    voteView.setVoteCount(voteCount);
  }

  public void setVoteDirection(int direction) {
    voteView.setVoteDirection(direction);
  }

  public void setAcceptedMarkIndicatorVisibility(boolean accepted) {
    tvAcceptedIndicator.setVisibility(accepted ? View.VISIBLE : View.GONE);
  }

  public void showAccept(boolean show) {
    tvMarkAsAccepted.setVisibility(show ? VISIBLE : GONE);
  }

  @OnLongClick(R.id.root_view)
  boolean onLongClick() {
    if (comment.isOwner()) {
      onCommentClickListener.onCommentLongClick(model, comment);
    }
    return true;
  }

  @OnClick({R.id.tv_comment_username, R.id.iv_comment_user_avatar})
  void onProfileClick() {
    onCommentClickListener.onCommentProfileClick(model, comment.getOwnerId());
  }

  public interface OnCommentClickListener {
    void onCommentLongClick(EpoxyModel<?> model, CommentRealm comment);

    void onCommentProfileClick(EpoxyModel<?> model, String userId);

    void onCommentVoteClick(EpoxyModel<?> model, String commentId, int direction);

    void onCommentMentionClick(EpoxyModel<?> model, String username);

    void onMarkAsAccepted(EpoxyModel<?> model, CommentRealm comment);
  }
}
