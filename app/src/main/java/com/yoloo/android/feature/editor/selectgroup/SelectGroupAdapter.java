package com.yoloo.android.feature.editor.selectgroup;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class SelectGroupAdapter extends EpoxyAdapter {

  private final RequestManager glide;
  private final CropCircleTransformation cropCircleTransformation;

  private OnItemClickListener<GroupRealm> onItemClickListener;

  SelectGroupAdapter(RequestManager glide, Context context) {
    this.glide = glide;
    cropCircleTransformation = new CropCircleTransformation(context);
  }

  public void setOnItemClickListener(OnItemClickListener<GroupRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void addGroups(List<GroupRealm> groups, boolean clear) {
    if (clear) {
      removeAllModels();
    }

    for (GroupRealm group : groups) {
      addModel(createModel(group));
    }
  }

  private SelectGroupAdapter$GroupModel_ createModel(GroupRealm group) {
    return new SelectGroupAdapter$GroupModel_()
        .group(group)
        .glide(glide)
        .cropCircleTransformation(cropCircleTransformation)
        .onItemClickListener(onItemClickListener);
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
          .load(group.getBackgroundUrl())
          .bitmapTransform(cropCircleTransformation)
          .into(holder.ivSelectGroupImage);

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
}
