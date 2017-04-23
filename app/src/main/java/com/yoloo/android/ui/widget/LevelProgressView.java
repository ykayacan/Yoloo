package com.yoloo.android.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LevelProgressView extends AppCompatSeekBar {
  public LevelProgressView(Context context) {
    super(context);
  }

  public LevelProgressView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public LevelProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return false;
  }
}
