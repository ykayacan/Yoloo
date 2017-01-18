package com.yoloo.android.feature.category;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.sorter.CategorySorter;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

class CategoryPresenter extends MvpPresenter<CategoryView> {

  private final CategoryRepository categoryRepository;

  CategoryPresenter(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Override
  public void onAttachView(CategoryView view) {
    super.onAttachView(view);
    loadCategories();
  }

  private void loadCategories() {
    Disposable d = categoryRepository.list(100, CategorySorter.DEFAULT)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showCategories, this::showError);

    getDisposable().add(d);
  }

  private void showCategories(List<CategoryRealm> realms) {
    getView().onLoaded(realms);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}