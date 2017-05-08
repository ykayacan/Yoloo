package com.yoloo.android.feature.groupgridoverview;

import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

class GroupGridEpoxyController extends TypedEpoxyController<List<GroupRealm>> {

  private final OnItemClickListener<GroupRealm> onItemClickListener;

  GroupGridEpoxyController(OnItemClickListener<GroupRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  @Override
  protected void buildModels(List<GroupRealm> groupRealms) {
    Stream
        .of(groupRealms)
        .forEach(group -> new GroupGridEpoxyController$GroupGridModel_()
            .id(group.getId())
            .adapter(this)
            .category(group)
            .onItemClickListener(onItemClickListener)
            .addTo(this));
  }

  @EpoxyModelClass(layout = R.layout.item_group_grid)
  public abstract static class GroupGridModel
      extends EpoxyModelWithHolder<GroupGridModel.CategoryHolder> {

    @EpoxyAttribute GroupRealm category;
    @EpoxyAttribute(hash = false) OnItemClickListener<GroupRealm> onItemClickListener;
    @EpoxyAttribute(hash = false) GroupGridEpoxyController adapter;

    @Override
    public void bind(CategoryHolder holder) {
      Glide
          .with(holder.itemView.getContext())
          .load(category.getImageWithIconUrl() + "=s220-rw")
          .into(holder.ivCategoryBackground);

      holder.tvCategoryText.setText(category.getName());

      holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, category));
    }

    static class CategoryHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_group_bg) ImageView ivCategoryBackground;
      @BindView(R.id.tv_group_text) TextView tvCategoryText;
    }
  }
}
