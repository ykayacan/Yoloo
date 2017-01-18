package com.yoloo.android.feature.feed.common.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.base.BaseEpoxyHolder;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.ui.widget.CompatTextView;
import com.yoloo.android.feature.ui.widget.VoteView;
import com.yoloo.android.feature.ui.widget.tagview.TagView;
import com.yoloo.android.feature.ui.widget.timeview.ZamanTextView;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.ReadMoreUtil;
import com.yoloo.android.util.glide.CropCircleTransformation;
import com.yoloo.android.util.glide.RoundedCornersTransformation;
import java.util.List;

public class RichQuestionModel extends EpoxyModelWithHolder<RichQuestionModel.RichQuestionHolder> {

  @EpoxyAttribute
  PostRealm post;

  @EpoxyAttribute(hash = false)
  OnProfileClickListener onProfileClickListener;

  @EpoxyAttribute(hash = false)
  OnShareClickListener onShareClickListener;

  @EpoxyAttribute(hash = false)
  OnCommentClickListener onCommentClickListener;

  @EpoxyAttribute(hash = false)
  OnReadMoreClickListener onReadMoreClickListener;

  @EpoxyAttribute(hash = false)
  OnOptionsClickListener onOptionsClickListener;

  @EpoxyAttribute(hash = false)
  OnVoteClickListener onVoteClickListener;

  @EpoxyAttribute(hash = false)
  OnContentImageClickListener onContentImageClickListener;

  @Override
  protected RichQuestionHolder createNewHolder() {
    return new RichQuestionHolder();
  }

  @Override
  protected int getDefaultLayout() {
    return getLayout();
  }

  @Override
  public void bind(RichQuestionHolder holder, List<Object> payloads) {
    if (!payloads.isEmpty()) {
      if (payloads.get(0) instanceof PostRealm) {
        PostRealm post = (PostRealm) payloads.get(0);
        holder.voteView.setVotes(post.getVotes());
        holder.voteView.setCurrentStatus(post.getDir());
      }
    } else {
      super.bind(holder, payloads);
    }
  }

  @Override
  public void bind(RichQuestionHolder holder) {
    super.bind(holder);
    final Context context = holder.ivUserAvatar.getContext().getApplicationContext();

    Glide.with(context)
        .load(post.getAvatarUrl())
        .bitmapTransform(CropCircleTransformation.getInstance(context))
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvContent.setText(
        isNormal() ? ReadMoreUtil.readMoreContent(post.getContent()) : post.getContent());

    Glide.with(context)
        .load(post.getMediaUrl())
        .bitmapTransform(RoundedCornersTransformation.getInstance(context, 16, 0))
        .into(holder.ivContentImage);

    holder.tvComment.setText(CountUtil.format(post.getComments()));
    holder.voteView.setVotes(post.getVotes());
    holder.voteView.setCurrentStatus(post.getDir());

    if (holder.tagView != null) {
      holder.tagView.setData(post.getCategoryNames());
    }

    tintDrawables(holder);
    setupClickListeners(holder);
  }

  @Override
  public void unbind(RichQuestionHolder holder) {
    clearClickListeners(holder);
  }

  private void setupClickListeners(RichQuestionHolder holder) {
    holder.ivUserAvatar.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, post.getOwnerId()));
    holder.tvUsername.setOnClickListener(
        v -> onProfileClickListener.onProfileClick(v, post.getOwnerId()));
    holder.tvContent.setOnClickListener(
        v -> onReadMoreClickListener.onReadMoreClickListener(v, post.getId(),
            post.getAcceptedCommentId(), id()));
    holder.tvShare.setOnClickListener(v -> onShareClickListener.onShareClick(v));
    holder.tvComment.setOnClickListener(v -> {
      v.setTag(post.getComments());
      onCommentClickListener.onCommentClick(v, post.getId(), post.getAcceptedCommentId());
    });
    holder.ibOptions.setOnClickListener(
        v -> onOptionsClickListener.onOptionsClick(v, this, post.getId(), post.isSelf()));
    holder.voteView.setOnVoteEventListener(
        direction -> onVoteClickListener.onVoteClick(post.getId(), direction,
            OnVoteClickListener.VotableType.POST));
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

  private boolean isNormal() {
    return getLayout() == R.layout.item_question_rich;
  }

  static class RichQuestionHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_question_user_avatar)
    ImageView ivUserAvatar;

    @BindView(R.id.tv_question_username)
    TextView tvUsername;

    @BindView(R.id.tv_question_time)
    ZamanTextView tvTime;

    @BindView(R.id.tv_question_content)
    TextView tvContent;

    @BindView(R.id.iv_question_content_image)
    ImageView ivContentImage;

    @BindView(R.id.ib_question_options)
    ImageButton ibOptions;

    @BindView(R.id.tv_question_share)
    CompatTextView tvShare;

    @BindView(R.id.tv_question_comment)
    CompatTextView tvComment;

    @BindView(R.id.tv_question_vote)
    VoteView voteView;

    @Nullable
    @BindView(R.id.view_category)
    TagView tagView;
  }
}