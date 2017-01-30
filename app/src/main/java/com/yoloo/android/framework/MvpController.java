package com.yoloo.android.framework;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.framework.delegate.MvpConductorDelegateCallback;
import com.yoloo.android.framework.delegate.MvpConductorLifecycleListener;

public abstract class MvpController<V extends MvpView, P extends MvpPresenter<V>>
    extends BaseController implements MvpView, MvpConductorDelegateCallback<V, P> {

  private P presenter;

  {
    addLifecycleListener(new MvpConductorLifecycleListener<>(this));
  }

  public MvpController() {
  }

  public MvpController(@Nullable Bundle args) {
    super(args);
  }

  @NonNull @Override public P getPresenter() {
    return presenter;
  }

  @Override public void setPresenter(@NonNull P presenter) {
    this.presenter = presenter;
  }

  @NonNull @Override public V getMvpView() {
    return (V) this;
  }
}