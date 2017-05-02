package com.yoloo.android.feature.editor.selectgroup;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class SelectGroupEpoxyController extends TypedEpoxyController<List<GroupRealm>> {

  private final RequestManager glide;

  @AutoModel SelectGroupEpoxyController$GroupHeaderModel_ header;

  private OnItemClickListener<GroupRealm> onItemClickListener;

  SelectGroupEpoxyController(RequestManager glide) {
    this.glide = glide;
  }

  public void setOnItemClickListener(OnItemClickListener<GroupRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  @Override
  protected void buildModels(List<GroupRealm> groups) {
    header.layout(R.layout.item_select_group_header).addTo(this);

    Stream.of(groups).forEach(this::createModel);
  }

  private void createModel(GroupRealm group) {
    new SelectGroupEpoxyController$GroupModel_()
        .id(group.getId())
        .group(group)
        .glide(glide)
        .onItemClickListener(onItemClickListener)
        .addTo(this);
  }

  @EpoxyModelClass(layout = R.layout.item_select_group)
  static abstract class GroupModel extends EpoxyModelWithHolder<GroupModel.GroupViewHolder> {

    @EpoxyAttribute GroupRealm group;
    @EpoxyAttribute(hash = false) CropCircleTransformation cropCircleTransformation;
    @EpoxyAttribute(hash = false) RequestManager glide;
    @EpoxyAttribute(hash = false) OnItemClickListener<GroupRealm> onItemClickListener;

    @Override
    public void bind(GroupViewHolder holder) {
      super.bind(holder);
      glide
          .load(group.getImageWithIconUrl() + "=s100-rw")
          .asBitmap()
          .diskCacheStrategy(DiskCacheStrategy.SOURCE)
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource,
                GlideAnimation<? super Bitmap> glideAnimation) {
              RoundedBitmapDrawable rbd =
                  RoundedBitmapDrawableFactory.create(holder.itemView.getContext().getResources(),
                      resource);
              rbd.setCornerRadius(6f);
              holder.ivSelectGroupImage.setImageDrawable(rbd);
            }
          });

      holder.tvSelectGroupTitle.setText(group.getName());

      holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, group));
    }

    @Override
    public void unbind(GroupViewHolder holder) {
      super.unbind(holder);
      holder.itemView.setOnClickListener(null);
    }

    static class GroupViewHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_item_select_group_image) ImageView ivSelectGroupImage;
      @BindView(R.id.tv_item_select_group_title) TextView tvSelectGroupTitle;
    }
  }

  @EpoxyModelClass(layout = R.layout.item_select_group_header)
  static abstract class GroupHeaderModel extends SimpleEpoxyModel {
    public GroupHeaderModel() {
      super(R.layout.item_select_group_header);
    }
  }
}
