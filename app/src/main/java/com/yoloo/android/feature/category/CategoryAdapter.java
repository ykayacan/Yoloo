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
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;

import java.util.List;

import butterknife.BindView;

class CategoryAdapter extends SelectableAdapter {

  private final OnItemClickListener<CategoryRealm> onItemClickListener;

  CategoryAdapter(OnItemClickListener<CategoryRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  void addCategories(List<CategoryRealm> categories) {
    addModels(Stream.of(categories)
        .map(category -> new CategoryAdapter$CategoryModel_()
            .adapter(this)
            .category(category)
            .onItemClickListener(onItemClickListener))
        .toList());
  }

  List<CategoryRealm> getSelectedCategories() {
    return Stream.of(getSelectedItems())
        .select(CategoryModel.class)
        .map(CategoryModel::getCategory)
        .toList();
  }

  @EpoxyModelClass(layout = R.layout.item_category)
  public static abstract class CategoryModel
      extends EpoxyModelWithHolder<CategoryModel.CategoryHolder> {

    @EpoxyAttribute CategoryRealm category;
    @EpoxyAttribute(hash = false) OnItemClickListener<CategoryRealm> onItemClickListener;
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

    CategoryRealm getCategory() {
      return category;
    }

    static class CategoryHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_category_bg) ImageView ivCategoryBackground;
      @BindView(R.id.tv_category_text) TextView tvCategoryText;
      @BindView(R.id.view_category_checkmark) View checkmarkView;
    }
  }
}
