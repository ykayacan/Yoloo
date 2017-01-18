package com.yoloo.android.feature.base.framework;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import com.yoloo.android.util.Preconditions;
import io.reactivex.disposables.CompositeDisposable;

public abstract class MvpPresenter<V extends MvpView> {

  private CompositeDisposable disposable = new CompositeDisposable();
  private V view;

  /**
   * On attach view.
   *
   * @param view the view
   */
  @UiThread
  @CallSuper
  public void onAttachView(V view) {
    this.view = view;
  }

  /**
   * On detach view.
   */
  @UiThread
  @CallSuper
  public void onDetachView() {
    this.disposable.clear();
    this.view = null;
  }

  /**
   * Get the attached view.
   *
   * @return <code>null</code>, if view is not attached, otherwise the concrete view instance
   */
  @UiThread
  @Nullable
  public final V getView() {
    return Preconditions.checkNotNull(view, "View has been detached!");
  }

  /**
   * Gets disposable.
   *
   * @return the disposable
   */
  public CompositeDisposable getDisposable() {
    return disposable;
  }
}