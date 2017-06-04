package com.yoloo.android.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.glide.AvatarTarget;

public class CommentView extends ConstraintLayout {

  @BindView(R.id.iv_comment_user_avatar) AvatarView ivUserAvatar;
  @BindView(R.id.tv_comment_username) TextView tvUsername;
  @BindView(R.id.tv_comment_time) TimeTextView tvTime;
  @BindView(R.id.tv_comment_content) LinkableTextView tvContent;
  @BindView(R.id.tv_comment_accepted_indicator) TextView tvAcceptedIndicator;
  @BindView(R.id.tv_mark_as_accepted) TextView tvMarkAsAccepted;
  @BindView(R.id.tv_comment_vote) VoteView voteView;

  @BindDimen(R.dimen.padding_normal) int normalPadding;
  @BindDimen(R.dimen.padding_micro) int microPadding;

  private CommentCallbacks callbacks;

  private CommentRealm comment;

  public CommentView(Context context, AttributeSet attrs) {
    super(context, attrs);
    inflate(getContext(), R.layout.layout_commentview, this);
    ButterKnife.bind(this);
    setBackgroundColor(Color.WHITE);

    setPadding(normalPadding, normalPadding, normalPadding, microPadding);

    DrawableHelper
        .create()
        .withDrawable(tvMarkAsAccepted.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();
  }

  public void setComment(CommentRealm comment) {
    this.comment = comment;

    tvUsername.setText(comment.getUsername());
    tvTime.setTimeStamp(comment.getCreated().getTime() / 1000);
    tvContent.setText(comment.getContent());
    voteView.setVoteCount(comment.getVoteCount());
    voteView.setVoteDirection(comment.getVoteDir());

    tvAcceptedIndicator.setVisibility(comment.isAccepted() ? View.VISIBLE : View.GONE);
    tvMarkAsAccepted.setVisibility(comment.showAcceptButton() ? VISIBLE : GONE);
  }

  public void setUserAvatar(@NonNull RequestManager glide, @NonNull String avatarUrl) {
    //noinspection unchecked
    glide
        .load(avatarUrl)
        .placeholder(R.drawable.ic_player_72dp)
        .into(new AvatarTarget(ivUserAvatar));
  }

  public void setCommentCallbacks(CommentCallbacks callbacks) {
    this.callbacks = callbacks;

    setOnLongClickListener(v -> {
      callbacks.onCommentLongClickListener(comment);
      return comment.isOwner();
    });

    tvContent.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.MENTION) {
        callbacks.onCommentMentionClickListener(value);
      }
    });

    tvMarkAsAccepted.setOnClickListener(v -> {
      tvMarkAsAccepted.setVisibility(View.GONE);
      tvAcceptedIndicator.setVisibility(View.VISIBLE);

      callbacks.onCommentAcceptRequestClickListener(comment);
    });

    voteView.setOnVoteEventListener(dir -> callbacks.onCommentVoteClickListener(comment, dir));
  }

  @OnClick({ R.id.tv_comment_username, R.id.iv_comment_user_avatar })
  void onProfileClick() {
    callbacks.onCommentProfileClickListener(comment.getOwnerId());
  }
}
