package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import timber.log.Timber;

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
  private boolean isAnimatingOut = false;

  public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
    super();
  }

  @Override
  public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout,
      final FloatingActionButton child, final View directTargetChild, final View target,
      final int nestedScrollAxes) {
    // Ensure we react to vertical scrolling
    return true;
  }

  @Override
  public void onNestedScroll(final CoordinatorLayout coordinatorLayout,
      final FloatingActionButton child, final View target, final int dxConsumed,
      final int dyConsumed, final int dxUnconsumed, final int dyUnconsumed) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
        dyUnconsumed);
    Timber.d("Consumed: %s", dyConsumed);
    if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
      child.hide();
    } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
      child.show();
    }
  }
}
