package com.yoloo.android.feature.models.post;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.widget.AvatarView;
import com.yoloo.android.ui.widget.CompatTextView;
import com.yoloo.android.ui.widget.VoteView;
import com.yoloo.android.ui.widget.linkabletextview.LinkableTextView;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.ReadMoreUtil;
import com.yoloo.android.util.glide.AvatarTarget;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_feed_question_rich)
public abstract class RichPostModel extends EpoxyModelWithHolder<RichPostModel.PostHolder> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute boolean detailLayout;
  @EpoxyAttribute(DoNotHash) PostCallbacks callbacks;
  @EpoxyAttribute(DoNotHash) RequestManager glide;
  @EpoxyAttribute(DoNotHash) ConstraintSet set;

  @Override public void bind(PostHolder holder) {
    super.bind(holder);
    tintDrawables(holder);

    //noinspection unchecked
    glide
        .load(post.getAvatarUrl().replace("s96-c", "s64-c-rw"))
        .placeholder(R.drawable.ic_player_72dp)
        .into(new AvatarTarget(holder.ivUserAvatar));

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvGroupName.setText(
        holder.itemView.getContext().getString(R.string.feed_item_group_name, post.getGroupId()));

    holder.tvBounty.setVisibility(post.getBounty() == 0 ? View.GONE : View.VISIBLE);
    holder.tvBounty.setText(String.valueOf(post.getBounty()));

    holder.tvContent.setText(
        detailLayout ? post.getContent() : ReadMoreUtil.addReadMore(post.getContent(), 200));

    holder.tvComment.setText(CountUtil.formatCount(post.getCommentCount()));

    holder.voteView.setVoteCount(post.getVoteCount());
    holder.voteView.setVoteDirection(post.getVoteDir());

    holder.ibOptions.setImageResource(
        post.isOwner() ? R.drawable.ic_more_vert_black_24dp : R.drawable.ic_bookmark_black_24dp);
    int color = ContextCompat.getColor(holder.itemView.getContext(),
        post.isBookmarked() ? R.color.primary : android.R.color.secondary_text_dark);
    holder.ibOptions.setColorFilter(color, PorterDuff.Mode.SRC_IN);

    if (holder.tvTags != null) {
      final Context context = holder.itemView.getContext();
      String tag = Stream.of(post.getTagNames())
          .map(tagName -> context.getString(R.string.label_tag, tagName))
          .collect(Collectors.joining(" "));
      holder.tvTags.setText(tag);
    }

    final int w = detailLayout ? 320 : 80;
    final int h = detailLayout ? 180 : 80;

    if (detailLayout) {
      glide
          .load(post.getMedias().get(0).getMediumSizeUrl())
          .override(w, h)
          .into(holder.ivContentImage);
    } else {
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
                  RoundedBitmapDrawableFactory.create(holder.itemView.getContext().getResources(),
                      resource);
              rbd.setCornerRadius(6f);
              holder.ivContentImage.setImageDrawable(rbd);
            }
          });
    }

    if (!detailLayout) {
      ConstraintLayout cl = (ConstraintLayout) holder.itemView;

      holder.itemView
          .getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
              int layoutRes = isContentTextExceedsImage(holder)
                  ? R.id.tv_item_feed_content
                  : R.id.iv_item_feed_cover;

              set.clone(cl);
              set.connect(R.id.tv_item_feed_share, ConstraintSet.TOP, layoutRes,
                  ConstraintSet.BOTTOM, 0);
              set.applyTo(cl);

              holder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
              return true;
            }
          });
    }

    // Listeners
    holder.ivUserAvatar.setOnClickListener(
        v -> callbacks.onPostProfileClickListener(post.getOwnerId()));

    holder.tvUsername.setOnClickListener(
        v -> callbacks.onPostProfileClickListener(post.getOwnerId()));

    holder.itemView.setOnClickListener(v -> {
      if (!detailLayout) {
        callbacks.onPostClickListener(post);
      }
    });

    holder.ivContentImage.setOnClickListener(
        v -> callbacks.onPostContentImageClickListener(post.getMedias().get(0)));

    holder.tvShare.setOnClickListener(v -> callbacks.onPostShareClickListener(post));

    holder.tvComment.setOnClickListener(v -> callbacks.onPostCommentClickListener(post));

    holder.voteView.setOnVoteEventListener(dir -> callbacks.onPostVoteClickListener(post, dir));

    holder.ibOptions.setOnClickListener(v -> {
      if (post.isOwner()) {
        callbacks.onPostOptionsClickListener(v, post);
      } else {
        int reversedColor = ContextCompat.getColor(holder.itemView.getContext(),
            post.isBookmarked() ? android.R.color.secondary_text_dark : R.color.primary);
        holder.ibOptions.setColorFilter(reversedColor, PorterDuff.Mode.SRC_IN);
        callbacks.onPostBookmarkClickListener(post);
      }
    });

    if (holder.tvTags != null) {
      holder.tvTags.setOnLinkClickListener((type, value) -> {
        if (type == LinkableTextView.Link.HASH_TAG) {
          callbacks.onPostTagClickListener(value);
        }
      });
    }
  }

  @Override public void unbind(PostHolder holder) {
    super.unbind(holder);
    Glide.clear(holder.ivContentImage);
    Glide.clear(holder.ivUserAvatar);
    holder.ivContentImage.setImageDrawable(null);
    holder.ivUserAvatar.setImageDrawable(null);

    holder.itemView.setOnClickListener(null);
    holder.ivUserAvatar.setOnClickListener(null);
    holder.tvUsername.setOnClickListener(null);
    holder.tvShare.setOnClickListener(null);
    holder.tvComment.setOnClickListener(null);
    holder.ibOptions.setOnClickListener(null);
  }

  private void tintDrawables(PostHolder holder) {
    final Context context = holder.itemView.getContext();

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

  private boolean isContentTextExceedsImage(PostHolder holder) {
    return holder.tvContent.getMeasuredHeight() > holder.ivContentImage.getMeasuredHeight();
  }

  static class PostHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) AvatarView ivUserAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
    @BindView(R.id.tv_item_feed_time) TimeTextView tvTime;
    @BindView(R.id.tv_item_feed_group_name) TextView tvGroupName;
    @BindView(R.id.tv_item_feed_bounty) TextView tvBounty;
    @BindView(R.id.ib_item_feed_options) ImageButton ibOptions;
    @BindView(R.id.tv_item_feed_content) TextView tvContent;
    @BindView(R.id.tv_item_feed_share) CompatTextView tvShare;
    @BindView(R.id.tv_item_feed_comment) CompatTextView tvComment;
    @BindView(R.id.tv_item_feed_vote) VoteView voteView;
    @BindView(R.id.iv_item_feed_cover) ImageView ivContentImage;
    @Nullable @BindView(R.id.container_item_feed_tags) LinkableTextView tvTags;
  }
}
