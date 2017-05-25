package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;

public class BlogView extends LinearLayout {

  @BindView(R.id.tv_blog_title) TextView tvBlogTitle;
  @BindView(R.id.tv_blog_user_info) TextView tvBlogUserInfo;
  @BindView(R.id.tv_item_feed_group_name) TextView tvGroupName;
  @BindView(R.id.tv_blog_content) TextView tvBlogContent;
  @BindView(R.id.tv_item_feed_share) TextView tvShare;
  @BindView(R.id.tv_item_feed_comment) TextView tvComment;
  @BindView(R.id.tv_item_feed_vote) VoteView voteView;
  @BindView(R.id.container_item_feed_tags) LinkableTextView tvTags;

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
    tvGroupName.setText(post.getGroupId());

    String tag = Stream.of(post.getTagNames())
        .map(tagName -> getContext().getString(R.string.label_tag, tagName))
        .collect(Collectors.joining(" "));
    tvTags.setText(tag);
  }

  public void setBlogTitle(@NonNull String title) {
    tvBlogTitle.setText(title);
  }

  public void setBlogUserInfo(@NonNull String username, @NonNull String levelTitle) {
    tvBlogUserInfo.setText(
        getResources().getString(R.string.label_blog_username_info, username, levelTitle));
  }

  public void setPostCallbacks(PostCallbacks callbacks, PostRealm post) {
    tvShare.setOnClickListener(v -> callbacks.onPostShareClickListener(post));
    tvComment.setOnClickListener(v -> callbacks.onPostCommentClickListener(post));
    voteView.setOnVoteEventListener(
        direction -> callbacks.onPostVoteClickListener(post, direction));
    tvTags.setOnLinkClickListener((type, value) -> {
      if (type == LinkableTextView.Link.HASH_TAG) {
        callbacks.onPostTagClickListener(value);
      }
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
