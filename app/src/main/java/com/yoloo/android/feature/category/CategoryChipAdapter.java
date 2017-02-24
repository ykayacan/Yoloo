package com.yoloo.android.feature.category;

import com.annimon.stream.Stream;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import java.util.List;

public class CategoryChipAdapter extends SelectableAdapter {

  private final OnItemClickListener<CategoryRealm> onItemClickListener;

  public CategoryChipAdapter(OnItemClickListener<CategoryRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void addCategories(List<CategoryRealm> categories) {
    Stream.of(categories)
        .forEach(category -> addModel(new CategoryChipModel_()
            .category(category)
            .adapter(this)
            .onItemClickListener(onItemClickListener)));
  }

  public List<CategoryRealm> getSelectedCategories() {
    return Stream.of(getSelectedItems())
        .select(CategoryChipModel.class)
        .map(CategoryChipModel::getCategory)
        .toList();
  }
}
