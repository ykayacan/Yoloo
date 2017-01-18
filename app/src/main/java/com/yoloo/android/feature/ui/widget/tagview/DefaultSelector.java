package com.yoloo.android.feature.ui.widget.tagview;

/**
 * Created by greenfrvr
 */
class DefaultSelector<T> implements TagView.DataSelector<T> {

  private DefaultSelector() {
  }

  public static DefaultSelector create() {
    return new DefaultSelector<>();
  }

  @Override
  public boolean preselect(T item) {
    return false;
  }
}
