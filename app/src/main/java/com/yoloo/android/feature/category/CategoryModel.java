package com.yoloo.android.feature.category;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.ui.recyclerview.BaseEpoxyHolder;

public class CategoryModel extends EpoxyModelWithHolder<CategoryModel.CategoryHolder> {

  @EpoxyAttribute CategoryRealm realm;

  @EpoxyAttribute(hash = false) CategoryAdapter.OnCategoryClickListener onCategoryClickListener;

  @EpoxyAttribute(hash = false) CategoryAdapter adapter;

  @Override protected CategoryHolder createNewHolder() {
    return new CategoryHolder();
  }

  @Override protected int getDefaultLayout() {
    return R.layout.item_category;
  }

  @Override public void bind(CategoryHolder holder) {
    Glide.with(holder.ivCategoryBackground.getContext())
        .load(realm.getBackgroundUrl())
        .into(holder.ivCategoryBackground);

    holder.tvCategoryText.setText(realm.getName());

    final boolean isSelected = adapter.isSelected(this);
    holder.ivCategoryBackground.setSelected(isSelected);
    holder.tvCategoryText.setSelected(isSelected);
    holder.checkmarkView.setVisibility(isSelected ? View.VISIBLE : View.GONE);

    holder.rootView.setOnClickListener(v -> {
      if (adapter.isMultiSelection() && adapter.canSelectItem(this)) {
        adapter.toggleSelection(this);
      }

      onCategoryClickListener.onCategoryClick(v, realm.getId(), realm.getName(),
          adapter.isMultiSelection());
    });
  }

  public CategoryRealm getRealm() {
    return realm;
  }

  static class CategoryHolder extends BaseEpoxyHolder {
    @BindView(R.id.root_view) ViewGroup rootView;

    @BindView(R.id.iv_category_bg) ImageView ivCategoryBackground;

    @BindView(R.id.tv_category_text) TextView tvCategoryText;

    @BindView(R.id.view_category_checkmark) View checkmarkView;
  }
}