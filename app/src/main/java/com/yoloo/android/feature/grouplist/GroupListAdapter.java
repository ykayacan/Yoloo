package com.yoloo.android.feature.grouplist;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import java.util.List;

public class GroupListAdapter extends EpoxyAdapter {

  private final RequestManager glide;

  private OnItemClickListener<GroupRealm> onItemClickListener;
  private OnSubscribeListener onSubscribeClickListener;

  public GroupListAdapter(RequestManager glide) {
    this.glide = glide;
  }

  public void setOnItemClickListener(OnItemClickListener<GroupRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void setOnSubscribeClickListener(OnSubscribeListener onSubscribeClickListener) {
    this.onSubscribeClickListener = onSubscribeClickListener;
  }

  public void addGroups(List<GroupRealm> groups) {
    for (GroupRealm group : groups) {
      addModel(createModel(group));
    }
  }

  public void clear() {
    removeAllModels();
  }

  private GroupListAdapter$GroupListItemModel_ createModel(GroupRealm group) {
    return new GroupListAdapter$GroupListItemModel_()
        .group(group)
        .glide(glide)
        .onItemClickListener(onItemClickListener)
        .onSubscribeClickListener(onSubscribeClickListener);
  }

  public interface OnSubscribeListener {
    void onSubscribe(@NonNull String groupId, boolean subscribed);
  }

  @EpoxyModelClass(layout = R.layout.item_group)
  public static abstract class GroupListItemModel
      extends EpoxyModelWithHolder<GroupListItemModel.GroupListHolder> {

    @EpoxyAttribute GroupRealm group;
    @EpoxyAttribute(hash = false) RequestManager glide;
    @EpoxyAttribute(hash = false) OnItemClickListener<GroupRealm> onItemClickListener;
    @EpoxyAttribute(hash = false) OnSubscribeListener onSubscribeClickListener;

    @Override
    public void bind(GroupListHolder holder, List<Object> payloads) {
      if (!payloads.isEmpty()) {
        if (payloads.get(0) instanceof GroupRealm) {
          GroupRealm payload = (GroupRealm) payloads.get(0);

          if (group.isSubscribed() != payload.isSubscribed()) {
            int buttonText =
                group.isSubscribed() ? R.string.group_unsubscribed : R.string.group_subscribe;
            holder.btnSubscribe.setText(buttonText);
          }
        }
      } else {
        super.bind(holder, payloads);
      }
      super.bind(holder, payloads);
    }

    @Override
    public void bind(GroupListHolder holder) {
      super.bind(holder);
      final Context context = holder.itemView.getContext();

      glide
          .load(group.getBackgroundUrl())
          .asBitmap()
          .diskCacheStrategy(DiskCacheStrategy.SOURCE)
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource,
                GlideAnimation<? super Bitmap> glideAnimation) {
              RoundedBitmapDrawable rbd =
                  RoundedBitmapDrawableFactory.create(context.getResources(), resource);
              rbd.setCornerRadius(24F);
              holder.ivSmallImage.setImageDrawable(rbd);
            }
          });

      holder.tvTitle.setText(group.getName());
      holder.tvSubscriberCount.setText(CountUtil.formatCount(group.getSubscriberCount()));
      DrawableHelper
          .create()
          .withDrawable(holder.tvSubscriberCount.getCompoundDrawables()[0])
          .withColor(ContextCompat.getColor(context, android.R.color.secondary_text_dark))
          .tint();

      int buttonText = group.isSubscribed() ? R.string.group_unsubscribed : R.string.group_subscribe;
      holder.btnSubscribe.setText(buttonText);
      holder.btnSubscribe.setSelected(group.isSubscribed());

      holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, group));
      holder.btnSubscribe.setOnClickListener(v -> {
        group.setSubscribed(!group.isSubscribed());
        holder.btnSubscribe.setSelected(!group.isSubscribed());
        holder.btnSubscribe.setText(
            group.isSubscribed() ? R.string.group_unsubscribed : R.string.group_subscribe);
        onSubscribeClickListener.onSubscribe(group.getId(), !group.isSubscribed());
      });
    }

    @Override
    public void unbind(GroupListHolder holder) {
      super.unbind(holder);
      holder.itemView.setOnClickListener(null);
      holder.btnSubscribe.setOnClickListener(null);
    }

    public GroupRealm getGroup() {
      return group;
    }

    static class GroupListHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_small_image) ImageView ivSmallImage;
      @BindView(R.id.tv_title) TextView tvTitle;
      @BindView(R.id.tv_subscriber_count) TextView tvSubscriberCount;
      @BindView(R.id.btn_subscribe) Button btnSubscribe;
    }
  }
}
