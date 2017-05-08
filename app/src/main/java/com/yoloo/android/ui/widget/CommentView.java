package com.yoloo.android.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.DrawableHelper;

public class CommentView extends ConstraintLayout {

  @BindView(R.id.iv_comment_user_avatar) ImageView ivUserAvatar;
  @BindView(R.id.tv_comment_username) TextView tvUsername;
  @BindView(R.id.tv_comment_time) TimeTextView tvTime;
  @BindView(R.id.tv_comment_content) LinkableTextView tvContent;
  @BindView(R.id.tv_comment_accepted_indicator) TextView tvAcceptedIndicator;
  @BindView(R.id.tv_mark_as_accepted) TextView tvMarkAsAccepted;
  @BindView(R.id.tv_comment_vote) VoteView voteView;

  @BindDimen(R.dimen.padding_normal) int normalPadding;
  @BindDimen(R.dimen.padding_micro) int microPadding;

  private OnCommentClickListener onCommentClickListener;
  private CommentRealm comment;

  public CommentView(Context context, AttributeSet attrs) {
    super(context, attrs);
    inflate(getContext(), R.layout.layout_commentview, this);
    ButterKnife.bind(this);

    setPadding(normalPadding, normalPadding, normalPadding, microPadding);


    DrawableHelper
        .create()
        .withDrawable(tvMarkAsAccepted.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();
  }

  public void setComment(CommentRealm comment, boolean showAccept) {
    this.comment = comment;

    tvUsername.setText(comment.getUsername());
    tvTime.setTimeStamp(comment.getCreated().getTime() / 1000);
    tvContent.setText(comment.getContent());
    voteView.setVoteCount(comment.getVoteCount());
    voteView.setVoteDirection(comment.getVoteDir());

    tvAcceptedIndicator.setVisibility(comment.isAccepted() ? View.VISIBLE : View.GONE);
    tvMarkAsAccepted.setVisibility(showAccept ? VISIBLE : GONE);
  }

  public void setUserAvatar(@NonNull RequestManager glide,
      @NonNull Transformation<Bitmap> transformation, @NonNull String avatarUrl) {
    //noinspection unchecked
    glide
        .load(avatarUrl)
        .bitmapTransform(transformation)
        .placeholder(R.drawable.ic_player_72dp)
        .into(ivUserAvatar);
  }

  public void setOnCommentClickListener(OnCommentClickListener onCommentClickListener) {
    this.onCommentClickListener = onCommentClickListener;

    setOnLongClickListener(v -> {
      onCommentClickListener.onCommentLongClick(comment);
      return comment.isOwner();
    });

    tvContent.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.MENTION) {
        onCommentClickListener.onCommentMentionClick(value);
      }
    });


    tvMarkAsAccepted.setOnClickListener(v -> {
      comment.setAccepted(true);
      tvMarkAsAccepted.setVisibility(View.GONE);
      tvAcceptedIndicator.setVisibility(View.VISIBLE);

      onCommentClickListener.onMarkAsAccepted(comment);
    });

    voteView.setOnVoteEventListener(direction -> {
      comment.setVoteDir(direction);
      onCommentClickListener.onCommentVoteClick(comment.getId(), direction);
    });
  }

  @OnClick({ R.id.tv_comment_username, R.id.iv_comment_user_avatar })
  void onProfileClick() {
    onCommentClickListener.onCommentProfileClick(comment.getOwnerId());
  }

  public interface OnCommentClickListener {
    void onCommentLongClick(CommentRealm comment);

    void onCommentProfileClick(String userId);

    void onCommentVoteClick(String commentId, int direction);

    void onCommentMentionClick(String username);

    void onMarkAsAccepted(CommentRealm comment);
  }
}
