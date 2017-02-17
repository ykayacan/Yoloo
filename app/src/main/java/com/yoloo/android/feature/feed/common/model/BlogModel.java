package com.yoloo.android.feature.feed.common.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.widget.CompatTextView;
import com.yoloo.android.ui.widget.VoteView;
import com.yoloo.android.ui.widget.tagview.TagView;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.ReadMoreUtil;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_feed_blog)
public abstract class BlogModel extends EpoxyModelWithHolder<BlogModel.BlogHolder> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) OnShareClickListener onShareClickListener;
  @EpoxyAttribute(hash = false) OnCommentClickListener onCommentClickListener;
  @EpoxyAttribute(hash = false) OnReadMoreClickListener onReadMoreClickListener;
  @EpoxyAttribute(hash = false) OnPostOptionsClickListener onPostOptionsClickListener;
  @EpoxyAttribute(hash = false) OnVoteClickListener onVoteClickListener;
  @EpoxyAttribute(hash = false) CropCircleTransformation circleTransformation;

  @Override public void bind(BlogHolder holder, List<Object> payloads) {
    if (!payloads.isEmpty()) {
      if (payloads.get(0) instanceof PostRealm) {
        PostRealm post = (PostRealm) payloads.get(0);
        holder.voteView.setVotes(post.getVotes());
        holder.voteView.setCurrentStatus(post.getDir());

        long comments = this.post.getComments();
        comments += 1;
        holder.tvComment.setText(CountUtil.format(comments));
      }
    } else {
      super.bind(holder, payloads);
    }
  }

  @Override public void bind(BlogHolder holder) {
    final Context context = holder.itemView.getContext();

    Glide.with(context)
        .load(post.getAvatarUrl())
        .bitmapTransform(circleTransformation)
        .placeholder(R.drawable.ic_player)
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvBounty.setVisibility(View.GONE);
    holder.tvTitle.setText(post.getTitle());
    holder.tvContent.setText(isNormal()
        ? ReadMoreUtil.readMoreContent(post.getContent(), 135)
        : post.getContent());

    Glide.with(context)
        .load(post.getMediaUrl())
        .override(320, 180)
        .into(holder.ivBlogCover);

    holder.tvComment.setText(CountUtil.format(post.getComments()));
    holder.voteView.setVotes(post.getVotes());
    holder.voteView.setCurrentStatus(post.getDir());

    if (holder.tagView != null) {
      holder.tagView.setData(post.getCategoryNames());
    }

    tintDrawables(holder);
    setupClickListeners(holder);
  }

  @Override public void unbind(BlogHolder holder) {
    Glide.clear(holder.ivBlogCover);
    Glide.clear(holder.ivUserAvatar);
    holder.ivBlogCover.setImageDrawable(null);
    holder.ivUserAvatar.setImageDrawable(null);
    clearClickListeners(holder);
  }

  @Override public boolean shouldSaveViewState() {
    return true;
  }

  private void tintDrawables(BlogHolder holder) {
    DrawableHelper.withContext(holder.tvShare.getContext())
        .withDrawable(holder.tvShare.getCompoundDrawables()[0])
        .withColor(android.R.color.secondary_text_dark)
        .tint();

    DrawableHelper.withContext(holder.tvComment.getContext())
        .withDrawable(holder.tvComment.getCompoundDrawables()[0])
        .withColor(android.R.color.secondary_text_dark)
        .tint();
  }

  private void setupClickListeners(BlogHolder holder) {
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, post.getOwnerId()));

    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, post.getOwnerId()));

    if (onReadMoreClickListener != null && post.hasReadMore()) {
      holder.itemView.setOnClickListener(
          v -> onReadMoreClickListener.onReadMoreClickListener(v, post));
    }

    holder.tvShare.setOnClickListener(v -> onShareClickListener.onShareClick(v, post));

    holder.tvComment.setOnClickListener(v -> onCommentClickListener.onCommentClick(v, post));

    holder.ibOptions.setOnClickListener(
        v -> onPostOptionsClickListener.onPostOptionsClick(v, this, post));

    holder.voteView.setOnVoteEventListener(direction -> {
      post.setDir(direction);
      onVoteClickListener.onVoteClick(post.getId(), direction, OnVoteClickListener.Type.POST);
    });
  }

  private void clearClickListeners(BlogHolder holder) {
    holder.ivUserAvatar.setOnClickListener(null);
    holder.tvUsername.setOnClickListener(null);
    holder.tvContent.setOnClickListener(null);
    holder.tvShare.setOnClickListener(null);
    holder.tvComment.setOnClickListener(null);
    holder.ibOptions.setOnClickListener(null);
    holder.voteView.setOnVoteEventListener(null);
  }

  private boolean isNormal() {
    return getLayout() == R.layout.item_bounty;
  }

  public String getItemId() {
    return post.getId();
  }

  static class BlogHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
    @BindView(R.id.tv_item_feed_time) TimeTextView tvTime;
    @BindView(R.id.tv_item_feed_bounty) TextView tvBounty;
    @BindView(R.id.tv_item_blog_title) TextView tvTitle;
    @BindView(R.id.tv_item_blog_content) TextView tvContent;
    @BindView(R.id.iv_item_blog_cover) ImageView ivBlogCover;
    @BindView(R.id.ib_item_feed_options) ImageButton ibOptions;
    @BindView(R.id.tv_item_feed_share) CompatTextView tvShare;
    @BindView(R.id.tv_item_feed_comment) CompatTextView tvComment;
    @BindView(R.id.tv_item_feed_vote) VoteView voteView;
    @Nullable @BindView(R.id.tagview_item_question_rich_category) TagView tagView;
  }
}
