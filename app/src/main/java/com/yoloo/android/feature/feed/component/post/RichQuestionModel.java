package com.yoloo.android.feature.feed.component.post;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.content.res.AppCompatResources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
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

@EpoxyModelClass(layout = R.layout.item_feed_question_rich)
public abstract class RichQuestionModel
    extends BasePostModel<RichQuestionModel.RichQuestionHolder> {

  @EpoxyAttribute(hash = false) OnContentImageClickListener onContentImageClickListener;
  @EpoxyAttribute(hash = false) ConstraintSet set;

  @Override
  public void bind(RichQuestionHolder holder, List<Object> payloads) {
    if (!payloads.isEmpty()) {
      if (payloads.get(0) instanceof PostRealm) {
        PostRealm payload = (PostRealm) payloads.get(0);

        if (post.getVoteCount() != payload.getVoteCount()) {
          holder.voteView.setVotes(payload.getVoteCount());
          post.setVoteCount(payload.getVoteCount());
        }

        if (post.getVoteDir() != payload.getVoteDir()) {
          holder.voteView.setVoteDirection(payload.getVoteDir());
          post.setVoteDir(payload.getVoteDir());
        }

        if (post.getCommentCount() != payload.getCommentCount()) {
          holder.tvComment.setText(CountUtil.formatCount(payload.getCommentCount()));
        }

        post.setAcceptedCommentId(payload.getAcceptedCommentId());
      }
    } else {
      super.bind(holder, payloads);
    }
  }

  @Override
  public void bind(RichQuestionHolder holder) {
    final Context context = holder.itemView.getContext();

    glide
        .load(post.getAvatarUrl())
        .bitmapTransform(circleTransformation)
        .placeholder(R.drawable.ic_player_72dp)
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvBounty.setVisibility(post.getBounty() == 0 ? View.GONE : View.VISIBLE);
    holder.tvBounty.setText(String.valueOf(post.getBounty()));
    holder.tvContent.setText(
        isNormal() ? ReadMoreUtil.addReadMore(context, post.getContent(), 190) : post.getContent());

    final int w = isNormal() ? 80 : 320;
    final int h = isNormal() ? 80 : 180;

    if (isNormal()) {
      glide
          .load(post.getMedias().get(0).getThumbSizeUrl())
          .asBitmap()
          .override(w, h)
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .centerCrop()
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource,
                GlideAnimation<? super Bitmap> glideAnimation) {
              RoundedBitmapDrawable rbd =
                  RoundedBitmapDrawableFactory.create(context.getResources(), resource);
              rbd.setCornerRadius(6f);
              holder.ivContentImage.setImageDrawable(rbd);
            }
          });
    } else {
      glide.load(post.getMedias()).override(w, h).into(holder.ivContentImage);
    }

    holder.tvComment.setText(CountUtil.formatCount(post.getCommentCount()));

    if (isNormal()) {
      ConstraintLayout cl = (ConstraintLayout) holder.itemView;

      holder.itemView
          .getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
              int layoutRes = isContentTextExceedsImage(holder)
                  ? R.id.tv_item_feed_rich_content
                  : R.id.iv_item_feed_rich_cover;

              set.clone(cl);
              set.connect(R.id.tv_item_feed_share, ConstraintSet.TOP, layoutRes,
                  ConstraintSet.BOTTOM, 0);
              set.applyTo(cl);

              holder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
              return true;
            }
          });
    }

    holder.voteView.setVotes(post.getVoteCount());
    holder.voteView.setVoteDirection(post.getVoteDir());

    final int drawableIconRes =
        isSelf() ? R.drawable.ic_more_vert_black_24dp : R.drawable.ic_bookmark_black_24dp;
    holder.ibOptions.setImageDrawable(AppCompatResources.getDrawable(context, drawableIconRes));

    if (holder.tagContainer != null) {
      Stream.of(post.getTagNames()).forEach(tagName -> {
        final TextView tag = new TextView(YolooApp.getAppContext());
        tag.setText(context.getString(R.string.label_tag, tagName));
        tag.setGravity(Gravity.CENTER);
        tag.setPadding(16, 10, 16, 10);
        tag.setBackground(ContextCompat.getDrawable(context, R.drawable.dialog_tag_bg));
        TextViewUtil.setTextAppearance(tag, context, R.style.TextAppearance_AppCompat);

        holder.tagContainer.addView(tag);
      });
    }

    tintDrawables(holder, context);

    // listeners
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, post.getOwnerId()));

    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, post.getOwnerId()));

    if (onItemClickListener != null && post.shouldShowReadMore()) {
      holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, post));
      holder.tvContent.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, post));
    }

    holder.tvShare.setOnClickListener(v -> onShareClickListener.onShareClick(v, post));

    holder.tvComment.setOnClickListener(v -> onCommentClickListener.onCommentClick(v, post));

    holder.ibOptions.setOnClickListener(v -> {
      if (isSelf()) {
        onPostOptionsClickListener.onPostOptionsClick(v, this, post);
      } else {
        final boolean isBookmarked = holder.ibOptions.getTag() == Boolean.TRUE;
        final int colorRes = isBookmarked ? android.R.color.secondary_text_dark : R.color.primary;

        holder.ibOptions.setTag(!isBookmarked);
        holder.ibOptions.setColorFilter(ContextCompat.getColor(context, colorRes),
            PorterDuff.Mode.SRC_IN);
        onBookmarkClickListener.onBookmarkClick(post.getId(), isBookmarked);
      }
    });

    holder.voteView.setOnVoteEventListener(direction -> {
      post.setVoteDir(direction);
      onVoteClickListener.onVoteClick(post.getId(), direction, OnVoteClickListener.Type.POST);
    });

    holder.ivContentImage.setOnClickListener(
        v -> onContentImageClickListener.onContentImageClick(v, post.getMedias().get(0)));
  }

  @Override
  public void unbind(RichQuestionHolder holder) {
    Glide.clear(holder.ivUserAvatar);
    Glide.clear(holder.ivContentImage);
    holder.ivUserAvatar.setImageDrawable(null);
    holder.ivContentImage.setImageDrawable(null);
    clearClickListeners(holder);
  }

  @Override
  protected int getDetailLayoutRes() {
    return R.layout.item_feed_question_rich_detail;
  }

  private boolean isContentTextExceedsImage(RichQuestionHolder holder) {
    return holder.tvContent.getMeasuredHeight() > holder.ivContentImage.getMeasuredHeight();
  }

  private void tintDrawables(RichQuestionHolder holder, Context context) {
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

  private void clearClickListeners(RichQuestionHolder holder) {
    holder.itemView.setOnClickListener(null);
    holder.ivUserAvatar.setOnClickListener(null);
    holder.tvUsername.setOnClickListener(null);
    holder.tvContent.setOnClickListener(null);
    holder.tvShare.setOnClickListener(null);
    holder.tvComment.setOnClickListener(null);
    holder.ibOptions.setOnClickListener(null);
    holder.voteView.setOnVoteEventListener(null);
    holder.ivContentImage.setOnClickListener(null);
  }

  static class RichQuestionHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
    @BindView(R.id.tv_item_feed_time) TimeTextView tvTime;
    @BindView(R.id.tv_item_feed_bounty) TextView tvBounty;
    @BindView(R.id.ib_item_feed_options) ImageButton ibOptions;
    @BindView(R.id.tv_item_feed_rich_content) TextView tvContent;
    @BindView(R.id.iv_item_feed_rich_cover) ImageView ivContentImage;
    @BindView(R.id.tv_item_feed_share) CompatTextView tvShare;
    @BindView(R.id.tv_item_feed_comment) CompatTextView tvComment;
    @BindView(R.id.tv_item_feed_vote) VoteView voteView;
    @Nullable @BindView(R.id.container_item_feed_tags) ViewGroup tagContainer;
  }
}
