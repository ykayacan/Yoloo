package com.yoloo.android.feature.base;

import com.yoloo.android.feature.base.framework.MvpPresenter;
import com.yoloo.android.feature.base.framework.MvpView;

public interface PresenterFactory {

  <V extends MvpView, P extends MvpPresenter<V>> P create(Class<P> clazz);
}