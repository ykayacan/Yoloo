package com.yoloo.android.feature.feed.common.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
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

@EpoxyModelClass(layout = R.layout.item_feed_question_rich)
public abstract class RichQuestionModel
    extends EpoxyModelWithHolder<RichQuestionModel.RichQuestionHolder> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) OnShareClickListener onShareClickListener;
  @EpoxyAttribute(hash = false) OnCommentClickListener onCommentClickListener;
  @EpoxyAttribute(hash = false) OnReadMoreClickListener onReadMoreClickListener;
  @EpoxyAttribute(hash = false) OnPostOptionsClickListener onPostOptionsClickListener;
  @EpoxyAttribute(hash = false) OnVoteClickListener onVoteClickListener;
  @EpoxyAttribute(hash = false) OnContentImageClickListener onContentImageClickListener;
  @EpoxyAttribute(hash = false) CropCircleTransformation circleTransformation;
  @EpoxyAttribute(hash = false) ConstraintSet set;

  @Override public void bind(RichQuestionHolder holder, List<Object> payloads) {
    if (!payloads.isEmpty()) {
      if (payloads.get(0) instanceof PostRealm) {
        PostRealm post = (PostRealm) payloads.get(0);
        holder.voteView.setVotes(post.getVotes());
        holder.voteView.setCurrentStatus(post.getDir());

        long comments = this.post.getComments();
        comments += 1;
        holder.tvComment.setText(CountUtil.format(comments));

        if (!TextUtils.isEmpty(post.getAcceptedCommentId())) {
          this.post.setAcceptedCommentId(post.getAcceptedCommentId());
        }
      }
    } else {
      super.bind(holder, payloads);
    }
  }

  @Override public void bind(RichQuestionHolder holder) {
    final Context context = holder.itemView.getContext();

    Glide.with(context)
        .load(post.getAvatarUrl())
        .bitmapTransform(circleTransformation)
        .placeholder(R.drawable.ic_player)
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvBounty.setVisibility(post.getBounty() == 0 ? View.GONE : View.VISIBLE);
    holder.tvBounty.setText(String.valueOf(post.getBounty()));
    holder.tvContent.setText(isNormal()
        ? ReadMoreUtil.readMoreContent(post.getContent(), 190)
        : post.getContent());

    final int w = isNormal() ? 80 : 320;
    final int h = isNormal() ? 80 : 180;

    if (isNormal()) {
      Glide.with(context)
          .load(post.getMediaUrl())
          .asBitmap()
          .override(w, h)
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .centerCrop()
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource,
                GlideAnimation<? super Bitmap> glideAnimation) {
              RoundedBitmapDrawable circularBitmapDrawable =
                  RoundedBitmapDrawableFactory.create(context.getResources(), resource);
              circularBitmapDrawable.setCornerRadius(10f);
              holder.ivContentImage.setImageDrawable(circularBitmapDrawable);
            }
          });
    } else {
      Glide.with(context)
          .load(post.getMediaUrl())
          .override(w, h)
          .into(holder.ivContentImage);
    }

    holder.tvComment.setText(CountUtil.format(post.getComments()));

    if (isNormal()) {
      ConstraintLayout cl = (ConstraintLayout) holder.itemView;

      holder.itemView.getViewTreeObserver().addOnPreDrawListener(
          new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              if (holder.tvContent.getMeasuredHeight()
                  > holder.ivContentImage.getMeasuredHeight()) {
                set.clone(cl);
                set.connect(R.id.tv_item_feed_share, ConstraintSet.TOP,
                    R.id.tv_item_question_rich_content, ConstraintSet.BOTTOM, 0);
                set.applyTo(cl);
              } else {
                set.clone(cl);
                set.connect(R.id.tv_item_feed_share, ConstraintSet.TOP,
                    R.id.iv_item_question_rich_cover, ConstraintSet.BOTTOM, 0);
                set.applyTo(cl);
              }
              holder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
              return true;
            }
          });
    }

    holder.voteView.setVotes(post.getVotes());
    holder.voteView.setCurrentStatus(post.getDir());

    if (holder.tagView != null) {
      holder.tagView.setData(post.getTagNames());
    }

    tintDrawables(holder);
    setupClickListeners(holder);
  }

  private void tintDrawables(RichQuestionHolder holder) {
    DrawableHelper.withContext(holder.tvShare.getContext())
        .withDrawable(holder.tvShare.getCompoundDrawables()[0])
        .withColor(android.R.color.secondary_text_dark)
        .tint();

    DrawableHelper.withContext(holder.tvComment.getContext())
        .withDrawable(holder.tvComment.getCompoundDrawables()[0])
        .withColor(android.R.color.secondary_text_dark)
        .tint();
  }

  @Override public void unbind(RichQuestionHolder holder) {
    Glide.clear(holder.ivUserAvatar);
    Glide.clear(holder.ivContentImage);
    holder.ivUserAvatar.setImageDrawable(null);
    holder.ivContentImage.setImageDrawable(null);
    clearClickListeners(holder);
  }

  @Override public boolean shouldSaveViewState() {
    return true;
  }

  private void setupClickListeners(RichQuestionHolder holder) {
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, post.getOwnerId()));

    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, post.getOwnerId()));

    if (onReadMoreClickListener != null && post.hasReadMore()) {
      holder.tvContent.setOnClickListener(
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

    holder.ivContentImage.setOnClickListener(
        v -> onContentImageClickListener.onContentImageClick(v, post.getMediaUrl()));
  }

  private void clearClickListeners(RichQuestionHolder holder) {
    holder.ivUserAvatar.setOnClickListener(null);
    holder.tvUsername.setOnClickListener(null);
    holder.tvContent.setOnClickListener(null);
    holder.tvShare.setOnClickListener(null);
    holder.tvComment.setOnClickListener(null);
    holder.ibOptions.setOnClickListener(null);
    holder.voteView.setOnVoteEventListener(null);
    holder.ivContentImage.setOnClickListener(null);
  }

  private boolean isNormal() {
    return getLayout() == R.layout.item_feed_question_rich;
  }

  public String getItemId() {
    return post.getId();
  }

  static class RichQuestionHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
    @BindView(R.id.tv_item_feed_time) TimeTextView tvTime;
    @BindView(R.id.tv_item_feed_bounty) TextView tvBounty;
    @BindView(R.id.ib_item_feed_options) ImageButton ibOptions;
    @BindView(R.id.tv_item_question_rich_content) TextView tvContent;
    @BindView(R.id.iv_item_question_rich_cover) ImageView ivContentImage;
    @BindView(R.id.tv_item_feed_share) CompatTextView tvShare;
    @BindView(R.id.tv_item_feed_comment) CompatTextView tvComment;
    @BindView(R.id.tv_item_feed_vote) VoteView voteView;
    @Nullable @BindView(R.id.tagview_item_question_rich_category) TagView tagView;
  }
}
