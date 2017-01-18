package com.yoloo.android.feature.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;
import com.yoloo.android.util.WeakHandler;

public class DelayedMultiAutoCompleteTextView extends MultiAutoCompleteTextView {

  private static final int DEFAULT_DELAY = 250;

  private int delay;

  private WeakHandler handler = new WeakHandler();
  private Runnable runnable;

  public DelayedMultiAutoCompleteTextView(Context context) {
    super(context);
    setDelay(DEFAULT_DELAY);
  }

  public DelayedMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setDelay(DEFAULT_DELAY);
  }

  @Override
  public boolean isInEditMode() {
    return true;
  }

  @Override
  protected void performFiltering(CharSequence text, int keyCode) {
    handler.removeCallbacks(runnable);
    runnable = () -> DelayedMultiAutoCompleteTextView.super.performFiltering(text, keyCode);
    handler.postDelayed(runnable, delay);
  }

  public int getDelay() {
    return delay;
  }

  public void setDelay(int delay) {
    this.delay = delay;
  }
}
