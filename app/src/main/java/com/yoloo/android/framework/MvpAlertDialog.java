package com.yoloo.android.framework;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import com.yoloo.android.framework.delegate.ViewGroupDelegateCallback;
import com.yoloo.android.framework.delegate.ViewGroupMvpDelegate;
import com.yoloo.android.framework.delegate.ViewGroupMvpDelegateImpl;

public abstract class MvpAlertDialog<V extends MvpView, P extends MvpPresenter<V>>
    extends AlertDialog implements ViewGroupDelegateCallback<V, P>, MvpView {

  private P presenter;
  private ViewGroupMvpDelegate<V, P> mvpDelegate;

  protected MvpAlertDialog(@NonNull Context context) {
    super(context);
  }

  protected MvpAlertDialog(@NonNull Context context, @StyleRes int themeResId) {
    super(context, themeResId);
  }

  protected MvpAlertDialog(@NonNull Context context, boolean cancelable,
      @Nullable OnCancelListener cancelListener) {
    super(context, cancelable, cancelListener);
  }

  /**
   * Get the mvp delegate. This is internally used for creating presenter, attaching and detaching
   * view from presenter etc.
   *
   * <p><b>Please note that only one instance from mvp delegate should be used per android.view.View
   * instance</b>.
   * </p>
   *
   * @return {@link ViewGroupMvpDelegate}
   */
  @NonNull private ViewGroupMvpDelegate<V, P> getMvpDelegate() {
    if (mvpDelegate == null) {
      mvpDelegate = new ViewGroupMvpDelegateImpl<>(this);
    }

    return mvpDelegate;
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    getMvpDelegate().onAttachedToWindow();
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    getMvpDelegate().onDetachedFromWindow();
  }

  @Override public P getPresenter() {
    return presenter;
  }

  @Override public void setPresenter(P presenter) {
    this.presenter = presenter;
  }

  @Override public V getMvpView() {
    return (V) this;
  }
}
