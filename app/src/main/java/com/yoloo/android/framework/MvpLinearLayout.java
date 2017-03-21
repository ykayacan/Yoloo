package com.yoloo.android.framework;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.yoloo.android.framework.delegate.ViewGroupDelegateCallback;
import com.yoloo.android.framework.delegate.ViewGroupMvpDelegate;
import com.yoloo.android.framework.delegate.ViewGroupMvpDelegateImpl;

public abstract class MvpLinearLayout<V extends MvpView, P extends MvpPresenter<V>>
    extends LinearLayout implements ViewGroupDelegateCallback<V, P>, MvpView {

  private P presenter;
  private ViewGroupMvpDelegate<V, P> mvpDelegate;

  public MvpLinearLayout(Context context) {
    super(context);
  }

  public MvpLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MvpLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
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
