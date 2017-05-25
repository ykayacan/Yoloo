package com.yoloo.android.feature.models.post;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.widget.CompatTextView;
import com.yoloo.android.ui.widget.VoteView;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.ReadMoreUtil;
import com.yoloo.android.util.TextViewUtil;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_feed_blog)
public abstract class BlogPostModel extends EpoxyModelWithHolder<BlogPostModel.PostHolder> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute String groupName;
  @EpoxyAttribute boolean detailLayout;
  @EpoxyAttribute(DoNotHash) PostCallbacks callbacks;
  @EpoxyAttribute(DoNotHash) RequestManager glide;
  @EpoxyAttribute(DoNotHash) Transformation<Bitmap> transformation;
  @EpoxyAttribute(DoNotHash) ConstraintSet set;

  @Override public void bind(PostHolder holder) {
    super.bind(holder);
    tintDrawables(holder);

    //noinspection unchecked
    glide
        .load(post.getAvatarUrl().replace("s96-c", "s64-c-rw"))
        .bitmapTransform(transformation)
        .placeholder(R.drawable.ic_player_72dp)
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvGroupName.setText(
        holder.itemView.getResources().getString(R.string.feed_item_group_name, groupName));

    holder.tvBounty.setVisibility(post.getBounty() == 0 ? View.GONE : View.VISIBLE);
    holder.tvBounty.setText(String.valueOf(post.getBounty()));

    holder.tvTitle.setText(post.getTitle());

    holder.tvContent.setText(
        detailLayout ? post.getContent() : ReadMoreUtil.addReadMore(post.getContent(), 200));

    glide.load(post.getMedias().get(0).getMediumSizeUrl()).into(holder.ivContentImage);

    holder.tvComment.setText(CountUtil.formatCount(post.getCommentCount()));

    holder.voteView.setVoteCount(post.getVoteCount());
    holder.voteView.setVoteDirection(post.getVoteDir());

    holder.ibOptions.setImageResource(
        post.isOwner() ? R.drawable.ic_more_vert_black_24dp : R.drawable.ic_bookmark_black_24dp);
    int color = ContextCompat.getColor(holder.itemView.getContext(),
        post.isBookmarked() ? R.color.primary : android.R.color.secondary_text_dark);
    holder.ibOptions.setColorFilter(color, PorterDuff.Mode.SRC_IN);

    if (holder.tagContainer != null) {
      Stream.of(post.getTagNames()).forEach(tagName -> {
        final Context context = holder.itemView.getContext();

        final TextView tag = new TextView(YolooApp.getAppContext());
        tag.setText(context.getString(R.string.label_tag, tagName));
        tag.setGravity(Gravity.CENTER);
        tag.setPadding(16, 10, 16, 10);
        tag.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_tag_bg));
        TextViewUtil.setTextAppearance(tag, context, R.style.TextAppearance_AppCompat);

        holder.tagContainer.addView(tag);
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

  static class PostHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
    @BindView(R.id.tv_item_feed_time) TimeTextView tvTime;
    @BindView(R.id.tv_item_feed_group_name) TextView tvGroupName;
    @BindView(R.id.tv_item_feed_bounty) TextView tvBounty;
    @BindView(R.id.tv_item_feed_title) TextView tvTitle;
    @BindView(R.id.ib_item_feed_options) ImageButton ibOptions;
    @BindView(R.id.tv_item_blog_content) TextView tvContent;
    @BindView(R.id.tv_item_feed_share) CompatTextView tvShare;
    @BindView(R.id.tv_item_feed_comment) CompatTextView tvComment;
    @BindView(R.id.tv_item_feed_vote) VoteView voteView;
    @BindView(R.id.iv_item_feed_cover) ImageView ivContentImage;
    @Nullable @BindView(R.id.container_item_feed_tags) ViewGroup tagContainer;
  }
}
