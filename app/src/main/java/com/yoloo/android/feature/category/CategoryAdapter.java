package com.yoloo.android.feature.category;

import com.annimon.stream.Stream;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import java.util.List;

class CategoryAdapter extends SelectableAdapter {

  private final String categoryType;

  private final OnItemClickListener<CategoryRealm> onItemClickListener;

  CategoryAdapter(@CategoryType String categoryType,
      OnItemClickListener<CategoryRealm> onItemClickListener) {
    this.categoryType = categoryType;
    this.onItemClickListener = onItemClickListener;
  }

  void addCategories(List<CategoryRealm> categories) {
    Stream.of(categories)
        .filter(category -> category.getType().equals(categoryType))
        .forEach(category -> addModel(new CategoryModel_()
            .adapter(this)
            .category(category)
            .onItemClickListener(onItemClickListener)));
  }

  List<CategoryRealm> getSelectedCategories() {
    return Stream.of(getSelectedItems())
        .select(CategoryModel.class)
        .map(CategoryModel::getCategory)
        .toList();
  }
}