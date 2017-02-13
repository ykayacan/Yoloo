package com.yoloo.android.feature.category;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;

@EpoxyModelClass(layout = R.layout.item_category)
public abstract class CategoryModel extends EpoxyModelWithHolder<CategoryModel.CategoryHolder> {

  @EpoxyAttribute CategoryRealm category;
  @EpoxyAttribute(hash = false) OnItemClickListener<CategoryRealm> onItemClickListener;
  @EpoxyAttribute(hash = false) CategoryAdapter adapter;

  @Override public void bind(CategoryHolder holder) {
    Glide.with(holder.ivCategoryBackground.getContext().getApplicationContext())
        .load(category.getBackgroundUrl())
        .into(holder.ivCategoryBackground);

    holder.tvCategoryText.setText(category.getName());

    final boolean isSelected = adapter.isSelected(this);
    holder.ivCategoryBackground.setSelected(isSelected);
    holder.tvCategoryText.setSelected(isSelected);
    holder.checkmarkView.setVisibility(isSelected ? View.VISIBLE : View.GONE);

    holder.rootView.setOnClickListener(v -> {
      adapter.toggleSelection(this);
      onItemClickListener.onItemClick(v, this, category);
    });
  }

  public CategoryRealm getCategory() {
    return category;
  }

  static class CategoryHolder extends BaseEpoxyHolder {
    @BindView(R.id.root_view) ViewGroup rootView;
    @BindView(R.id.iv_category_bg) ImageView ivCategoryBackground;
    @BindView(R.id.tv_category_text) TextView tvCategoryText;
    @BindView(R.id.view_category_checkmark) View checkmarkView;
  }
}