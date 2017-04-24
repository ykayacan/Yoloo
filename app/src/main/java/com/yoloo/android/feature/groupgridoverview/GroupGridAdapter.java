package com.yoloo.android.feature.groupgridoverview;

import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

class GroupGridAdapter extends EpoxyAdapter {

  private final OnItemClickListener<GroupRealm> onItemClickListener;

  GroupGridAdapter(OnItemClickListener<GroupRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  void addCategories(List<GroupRealm> groups) {
    addModels(Stream
        .of(groups)
        .map(group -> new GroupGridAdapter$CategoryModel_()
            .adapter(this)
            .category(group)
            .onItemClickListener(onItemClickListener))
        .toList());
  }

  @EpoxyModelClass(layout = R.layout.item_category)
  public abstract static class CategoryModel
      extends EpoxyModelWithHolder<CategoryModel.CategoryHolder> {

    @EpoxyAttribute GroupRealm category;
    @EpoxyAttribute(hash = false) OnItemClickListener<GroupRealm> onItemClickListener;
    @EpoxyAttribute(hash = false) GroupGridAdapter adapter;

    @Override
    public void bind(CategoryHolder holder) {
      Glide
          .with(holder.itemView.getContext())
          .load(category.getBackgroundUrl() + "=s220-rw")
          .into(holder.ivCategoryBackground);

      holder.tvCategoryText.setText(category.getName());

      holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, category));
    }

    GroupRealm getCategory() {
      return category;
    }

    static class CategoryHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_category_bg) ImageView ivCategoryBackground;
      @BindView(R.id.tv_category_text) TextView tvCategoryText;
    }
  }
}
