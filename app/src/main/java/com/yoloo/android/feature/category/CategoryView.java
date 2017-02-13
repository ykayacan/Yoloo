package com.yoloo.android.feature.category;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface CategoryView extends MvpView {

  void onCategoriesLoaded(List<CategoryRealm> categories);

  void onError(Throwable throwable);
}
