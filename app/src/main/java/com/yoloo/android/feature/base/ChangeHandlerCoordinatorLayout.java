package com.yoloo.android.feature.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;

public class ChangeHandlerCoordinatorLayout extends CoordinatorLayout implements
    ControllerChangeHandler.ControllerChangeListener {
  private int inProgressTransactionCount;

  public ChangeHandlerCoordinatorLayout(Context context) {
    super(context);
  }

  public ChangeHandlerCoordinatorLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ChangeHandlerCoordinatorLayout(Context context, AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return this.inProgressTransactionCount > 0 || super.onInterceptTouchEvent(ev);
  }

  public void onChangeStarted(@Nullable Controller to, @Nullable Controller from, boolean isPush,
      @NonNull
          ViewGroup container, @NonNull ControllerChangeHandler handler) {
    ++this.inProgressTransactionCount;
  }

  public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from, boolean isPush,
      @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
    --this.inProgressTransactionCount;
  }
}
