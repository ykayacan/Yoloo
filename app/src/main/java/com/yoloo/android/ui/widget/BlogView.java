package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;

public class BlogView extends LinearLayout {

  @BindView(R.id.tv_blog_title) TextView tvBlogTitle;
  @BindView(R.id.tv_blog_user_info) TextView tvBlogUserInfo;
  @BindView(R.id.tv_blog_content) TextView tvBlogContent;
  @BindView(R.id.tv_item_feed_share) TextView tvShare;
  @BindView(R.id.tv_item_feed_comment) TextView tvComment;
  @BindView(R.id.tv_item_feed_vote) VoteView voteView;

  public BlogView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    inflate(getContext(), R.layout.layout_blogview, this);
    ButterKnife.bind(this);

    setOrientation(LinearLayout.VERTICAL);
    tintDrawables(context);
  }

  public void setPost(PostRealm post) {
    tvComment.setText(CountUtil.formatCount(post.getCommentCount()));
    voteView.setVoteCount(post.getVoteCount());
    voteView.setVoteDirection(post.getVoteDir());
  }

  public void setBlogTitle(@NonNull String title) {
    tvBlogTitle.setText(title);
  }

  public void setBlogUserInfo(@NonNull String username, @NonNull String levelTitle) {
    tvBlogUserInfo.setText(
        getResources().getString(R.string.label_blog_username_info, username, levelTitle));
  }

  public void setOnShareClickListener(OnShareClickListener onShareClickListener, PostRealm post) {
    tvShare.setOnClickListener(v -> onShareClickListener.onShareClick(v, post));
  }

  public void setOnCommentClickListener(OnCommentClickListener onCommentClickListener,
      PostRealm post) {
    tvComment.setOnClickListener(v -> onCommentClickListener.onCommentClick(v, post));
  }

  public void setOnVoteClickListener(OnVoteClickListener onVoteClickListener, PostRealm post) {
    voteView.setOnVoteEventListener(direction -> {
      post.setVoteDir(direction);
      onVoteClickListener.onPostVoteClick(post, direction);
    });
  }

  public void setBlogContent(@NonNull String content) {
    tvBlogContent.setText(content);
  }

  private void tintDrawables(Context context) {
    DrawableHelper
        .create()
        .withDrawable(tvShare.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();

    DrawableHelper
        .create()
        .withDrawable(tvComment.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();
  }
}
