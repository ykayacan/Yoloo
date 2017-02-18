package com.yoloo.android.feature.category;

import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.sorter.CategorySorter;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class CategoryPresenter extends MvpPresenter<CategoryView> {

  private final CategoryRepository categoryRepository;

  CategoryPresenter(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Override public void onAttachView(CategoryView view) {
    super.onAttachView(view);
    loadCategories();
  }

  private void loadCategories() {
    Disposable d = categoryRepository.listCategories(100, CategorySorter.DEFAULT)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(categories -> getView().onCategoriesLoaded(categories),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
