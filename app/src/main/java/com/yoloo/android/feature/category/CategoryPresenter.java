package com.yoloo.android.feature.category;

import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.sorter.CategorySorter;
import com.yoloo.android.framework.MvpPresenter;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class CategoryPresenter extends MvpPresenter<CategoryView> {

  private final CategoryRepository categoryRepository;

  CategoryPresenter(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  void loadCategories() {
    Disposable d = categoryRepository.listCategories(CategorySorter.DEFAULT, 100)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(categories -> getView().onCategoriesLoaded(categories),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadInterestedCategories(@Nonnull String userId) {
    Disposable d = categoryRepository.listInterestedCategories(userId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(categories -> getView().onCategoriesLoaded(categories),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
