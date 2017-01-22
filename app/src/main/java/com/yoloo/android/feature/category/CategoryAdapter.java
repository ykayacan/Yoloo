package com.yoloo.android.feature.category;

import android.view.View;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.ui.recyclerview.SelectableEpoxyAdapter;
import io.reactivex.Observable;
import java.util.List;

class CategoryAdapter extends SelectableEpoxyAdapter {

  private final String categoryType;

  private boolean multiSelection;

  private int maxSelectedItems;

  private OnCategoryClickListener onCategoryClickListener;

  CategoryAdapter(@CategoryType String categoryType) {
    this.categoryType = categoryType;
    this.maxSelectedItems = Integer.MAX_VALUE;
  }

  public boolean isMultiSelection() {
    return multiSelection;
  }

  public void setMultiSelection(boolean multiSelection) {
    this.multiSelection = multiSelection;
  }

  @Override public int getMaxSelectedItems() {
    return maxSelectedItems;
  }

  public void setMaxSelectedItems(int maxSelectedItems) {
    this.maxSelectedItems = maxSelectedItems;
  }

  public void setOnCategoryClickListener(OnCategoryClickListener onCategoryClickListener) {
    this.onCategoryClickListener = onCategoryClickListener;
  }

  public void addCategories(List<CategoryRealm> categories) {
    for (CategoryRealm realm : categories) {
      if (realm.getType().equals(categoryType)) {
        addModel(new CategoryModel_().adapter(this)
            .onCategoryClickListener(onCategoryClickListener)
            .realm(realm));
      }
    }
  }

  public List<CategoryRealm> getSelectedCategories() {
    return Observable.fromIterable(getSelectedItems())
        .cast(CategoryModel.class)
        .map(CategoryModel::getRealm)
        .toList()
        .blockingGet();
  }

  public interface OnCategoryClickListener {
    void onCategoryClick(View v, String categoryId, String name, boolean multiSelection);
  }
}