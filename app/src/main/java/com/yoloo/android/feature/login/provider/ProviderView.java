package com.yoloo.android.feature.login.provider;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

public interface ProviderView extends MvpView {

  void onCategoriesLoaded(List<CategoryRealm> categories);

  void onSignedUp();

  void onError(Throwable t);

  void onShowLoading();

  void onHideLoading();
}