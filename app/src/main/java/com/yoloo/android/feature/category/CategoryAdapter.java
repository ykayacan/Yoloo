package com.yoloo.android.feature.category;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;

import java.util.List;

import butterknife.BindView;

class CategoryAdapter extends SelectableAdapter {

  private final OnItemClickListener<GroupRealm> onItemClickListener;

  CategoryAdapter(OnItemClickListener<GroupRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  void addCategories(List<GroupRealm> categories) {
    addModels(Stream.of(categories)
        .map(category -> new CategoryAdapter$CategoryModel_()
            .adapter(this)
            .category(category)
            .onItemClickListener(onItemClickListener))
        .toList());
  }

  List<GroupRealm> getSelectedCategories() {
    return Stream.of(getSelectedItems())
        .select(CategoryModel.class)
        .map(CategoryModel::getCategory)
        .toList();
  }

  @EpoxyModelClass(layout = R.layout.item_category)
  public abstract static class CategoryModel
      extends EpoxyModelWithHolder<CategoryModel.CategoryHolder> {

    @EpoxyAttribute GroupRealm category;
    @EpoxyAttribute(hash = false) OnItemClickListener<GroupRealm> onItemClickListener;
    @EpoxyAttribute(hash = false) CategoryAdapter adapter;

    @Override public void bind(CategoryHolder holder) {
      Glide.with(holder.itemView.getContext())
          .load(category.getBackgroundUrl() + "=s220-rw")
          .into(holder.ivCategoryBackground);

      holder.tvCategoryText.setText(category.getName());

      final boolean isSelected = adapter.isSelected(this);
      holder.ivCategoryBackground.setSelected(isSelected);
      holder.tvCategoryText.setSelected(isSelected);
      holder.checkmarkView.setVisibility(isSelected ? View.VISIBLE : View.GONE);

      holder.itemView.setOnClickListener(v -> {
        adapter.toggleSelection(this);
        onItemClickListener.onItemClick(v, this, category);
      });
    }

    GroupRealm getCategory() {
      return category;
    }

    static class CategoryHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_category_bg) ImageView ivCategoryBackground;
      @BindView(R.id.tv_category_text) TextView tvCategoryText;
      @BindView(R.id.view_category_checkmark) View checkmarkView;
    }
  }
}
