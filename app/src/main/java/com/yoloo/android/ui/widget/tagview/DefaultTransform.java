package com.yoloo.android.ui.widget.tagview;

/**
 * Created by greenfrvr
 */
class DefaultTransform<T> implements TagView.DataTransform<T> {

  private DefaultTransform() {
  }

  public static DefaultTransform create() {
    return new DefaultTransform<>();
  }

  @Override
  public CharSequence prepare(T item) {
    return item.toString();
  }
}
