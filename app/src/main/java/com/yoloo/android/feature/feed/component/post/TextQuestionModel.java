package com.yoloo.android.feature.feed.component.post;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
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
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.ReadMoreUtil;
import com.yoloo.android.util.TextViewUtil;
import com.yoloo.android.util.VersionUtil;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

import java.util.List;

import butterknife.BindView;

@EpoxyModelClass(layout = R.layout.item_feed_question_normal)
public abstract class TextQuestionModel
    extends EpoxyModelWithHolder<TextQuestionModel.QuestionHolder> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) OnShareClickListener onShareClickListener;
  @EpoxyAttribute(hash = false) OnCommentClickListener onCommentClickListener;
  @EpoxyAttribute(hash = false) OnReadMoreClickListener onReadMoreClickListener;
  @EpoxyAttribute(hash = false) OnPostOptionsClickListener onPostOptionsClickListener;
  @EpoxyAttribute(hash = false) OnVoteClickListener onVoteClickListener;
  @EpoxyAttribute(hash = false) CropCircleTransformation circleTransformation;

  @Override public void bind(QuestionHolder holder, List<Object> payloads) {
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

  @Override public void bind(QuestionHolder holder) {
    final Context context = holder.itemView.getContext();

    Glide.with(context)
        .load(post.getAvatarUrl().replace("s96-c", "s64-c-rw"))
        .bitmapTransform(circleTransformation)
        .placeholder(R.drawable.ic_player)
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvBounty.setVisibility(post.getBounty() == 0 ? View.GONE : View.VISIBLE);
    holder.tvBounty.setText(String.valueOf(post.getBounty()));
    holder.tvContent.setText(isNormal()
        ? ReadMoreUtil.addReadMore(context, post.getContent(), 200)
        : post.getContent());
    holder.tvComment.setText(CountUtil.formatCount(post.getCommentCount()));
    holder.voteView.setVotes(post.getVoteCount());
    holder.voteView.setVoteDirection(post.getVoteDir());

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

    if (VersionUtil.hasL()) {
      holder.ivUserAvatar.setTransitionName(
          context.getResources().getString(R.string.transition_avatar));
      holder.tvUsername.setTransitionName(
          context.getResources().getString(R.string.transition_username, post.getOwnerId()));
    }

    setupClickListeners(holder);
  }

  @Override public void unbind(QuestionHolder holder) {
    Glide.clear(holder.ivUserAvatar);
    holder.ivUserAvatar.setImageDrawable(null);
    clearClickListeners(holder);
  }

  private void tintDrawables(QuestionHolder holder, Context context) {
    DrawableHelper.create()
        .withDrawable(holder.tvShare.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();

    DrawableHelper.create()
        .withDrawable(holder.tvComment.getCompoundDrawables()[0])
        .withColor(context, android.R.color.secondary_text_dark)
        .tint();
  }

  private void setupClickListeners(QuestionHolder holder) {
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, post.getOwnerId()));

    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, this, post.getOwnerId()));

    if (onReadMoreClickListener != null && post.shouldShowReadMore()) {
      holder.tvContent.setOnClickListener(
          v -> onReadMoreClickListener.onReadMoreClick(v, post));
    }

    holder.tvShare.setOnClickListener(v -> onShareClickListener.onShareClick(v, post));

    holder.tvComment.setOnClickListener(v -> onCommentClickListener.onCommentClick(v, post));

    holder.ibOptions.setOnClickListener(
        v -> onPostOptionsClickListener.onPostOptionsClick(v, this, post));

    holder.voteView.setOnVoteEventListener(direction -> {
      post.setVoteDir(direction);
      onVoteClickListener.onVoteClick(post.getId(), direction, OnVoteClickListener.Type.POST);
    });
  }

  private void clearClickListeners(QuestionHolder holder) {
    holder.ivUserAvatar.setOnClickListener(null);
    holder.tvUsername.setOnClickListener(null);
    holder.tvContent.setOnClickListener(null);
    holder.tvShare.setOnClickListener(null);
    holder.tvComment.setOnClickListener(null);
    holder.ibOptions.setOnClickListener(null);
    holder.voteView.setOnVoteEventListener(null);
  }

  private boolean isNormal() {
    return getLayout() == R.layout.item_feed_question_normal;
  }

  public String getItemId() {
    return post.getId();
  }

  static class QuestionHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
    @BindView(R.id.tv_item_feed_time) TimeTextView tvTime;
    @BindView(R.id.tv_item_feed_bounty) TextView tvBounty;
    @BindView(R.id.ib_item_feed_options) ImageButton ibOptions;
    @BindView(R.id.tv_item_feed_content) TextView tvContent;
    @BindView(R.id.tv_item_feed_share) CompatTextView tvShare;
    @BindView(R.id.tv_item_feed_comment) CompatTextView tvComment;
    @BindView(R.id.tv_item_feed_vote) VoteView voteView;
    @Nullable @BindView(R.id.container_item_feed_tags) ViewGroup tagContainer;
  }
}
