package com.yoloo.android.feature.feed.component.post;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyModelClass;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.widget.CompatTextView;
import com.yoloo.android.ui.widget.VoteView;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.ReadMoreUtil;
import com.yoloo.android.util.TextViewUtil;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_feed_blog)
public abstract class BlogModel extends BasePostModel<BlogModel.BlogHolder> {

  @Override
  public void bind(BlogHolder holder, List<Object> payloads) {
    if (!payloads.isEmpty()) {
      if (payloads.get(0) instanceof PostRealm) {
        PostRealm payload = (PostRealm) payloads.get(0);

        if (post.getVoteCount() != payload.getVoteCount()) {
          holder.voteView.setVoteCount(payload.getVoteCount());
          post.setVoteCount(payload.getVoteCount());
        }

        if (post.getVoteDir() != payload.getVoteDir()) {
          holder.voteView.setVoteDirection(payload.getVoteDir());
          post.setVoteDir(payload.getVoteDir());
        }

        post.setAcceptedCommentId(payload.getAcceptedCommentId());
      }
    } else {
      super.bind(holder, payloads);
    }
  }

  @Override
  public void bind(BlogHolder holder) {
    final Context context = holder.itemView.getContext();

    glide
        .load(post.getAvatarUrl())
        .bitmapTransform(circleTransformation)
        .placeholder(R.drawable.ic_player_72dp)
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvBounty.setVisibility(View.GONE);
    holder.tvTitle.setText(post.getTitle());
    holder.tvContent.setText(
        isNormal() ? ReadMoreUtil.addReadMore(context, post.getContent(), 135) : post.getContent());

    glide
        .load("http://www.adrenalinoutdoor"
            + ".com/images_buyuk/f62/The-North-Face-Mountain-25-Cadir_16762_2.jpg")
        .override(320, 180)
        .into(holder.ivBlogCover);

    holder.tvComment.setText(CountUtil.formatCount(post.getCommentCount()));
    holder.voteView.setVoteCount(post.getVoteCount());
    holder.voteView.setVoteDirection(post.getVoteDir());

    final int drawableIconRes =
        isSelf() ? R.drawable.ic_more_vert_black_24dp : R.drawable.ic_bookmark_black_24dp;
    holder.ibOptions.setImageDrawable(AppCompatResources.getDrawable(context, drawableIconRes));

    if (!isSelf()) {
      final int colorRes =
          post.isBookmarked() ? R.color.primary : android.R.color.secondary_text_dark;
      holder.ibOptions.setColorFilter(ContextCompat.getColor(context, colorRes),
          PorterDuff.Mode.SRC_IN);
    }

    if (holder.tagContainer != null) {
      Stream.of(post.getTagNames()).forEach(tagName -> {
        final TextView tag = new TextView(YolooApp.getAppContext());
        tag.setText(context.getString(R.string.label_tag, tagName));
        tag.setGravity(Gravity.CENTER);
        tag.setPadding(16, 10, 16, 10);
        //tag.setBackground(ContextCompat.getDrawable(context, R.drawable.dialog_tag_bg));
        TextViewUtil.setTextAppearance(tag, context, R.style.TextAppearance_AppCompat);

        holder.tagContainer.addView(tag);
      });
    }

    tintDrawables(holder, context);

    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, post.getOwnerId()));

    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, post.getOwnerId()));

    if (onItemClickListener != null && post.shouldShowReadMore()) {
      holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, post));
    }

    holder.tvShare.setOnClickListener(v -> onShareClickListener.onShareClick(v, post));

    holder.tvComment.setOnClickListener(v -> onCommentClickListener.onCommentClick(v, post));

    holder.ibOptions.setOnClickListener(v -> {
      if (isSelf()) {
        onPostOptionsClickListener.onPostOptionsClick(v, this, post);
      } else {
        final int reversedColorRes =
            post.isBookmarked() ? android.R.color.secondary_text_dark : R.color.primary;
        holder.ibOptions.setColorFilter(ContextCompat.getColor(context, reversedColorRes),
            PorterDuff.Mode.SRC_IN);
        post.setBookmarked(!post.isBookmarked());
        onBookmarkClickListener.onBookmarkClick(post.getId(), post.isBookmarked());
      }
    });

    holder.voteView.setOnVoteEventListener(direction -> {
      post.setVoteDir(direction);
      onVoteClickListener.onVoteClick(post.getId(), direction, OnVoteClickListener.Type.POST);
    });
  }

  @Override
  public void unbind(BlogHolder holder) {
    Glide.clear(holder.ivBlogCover);
    Glide.clear(holder.ivUserAvatar);
    holder.ivBlogCover.setImageDrawable(null);
    holder.ivUserAvatar.setImageDrawable(null);
    clearClickListeners(holder);
  }

  private void tintDrawables(BlogHolder holder, Context context) {
    DrawableHelper
        .create()
        .withDrawable(holder.tvShare.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();

    DrawableHelper
        .create()
        .withDrawable(holder.tvComment.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();
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

  @Override
  protected int getDetailLayoutRes() {
    return R.layout.item_feed_question_rich_detail;
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
    @Nullable @BindView(R.id.container_item_feed_tags) ViewGroup tagContainer;
  }
}
