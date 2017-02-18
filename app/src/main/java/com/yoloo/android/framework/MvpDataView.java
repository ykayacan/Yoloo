package com.yoloo.android.framework;

/**
 * The interface Mvp data view.
 *
 * @param <M> the type parameter
 */
public interface MvpDataView<M> extends MvpView {

  /**
   * Display a loading view while loading data in background.
   * <b>The loading view must have the id = R.id.loadingView</b>
   *
   * @param pullToRefresh true, if pull-to-refresh has been invoked loading.
   */
  void onLoading(boolean pullToRefresh);

  /**
   * The data that should be displayed.
   *
   * @param value the data
   */
  void onLoaded(M value);

  /**
   * Show the error view.
   * <b>The error view must be a TextView with the id = R.id.errorView</b>
   *
   * @param e The Throwable that has caused this error
   */
  void onError(Throwable e);

  /**
   * Show the empty view.
   * If no data is arrived then show the empty view.
   */
  void onEmpty();
}
