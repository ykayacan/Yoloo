package com.yoloo.android.ui.widget;

/*public class ScrollAwareFABBehavior extends FabSpeedDialBehaviour {
  private boolean isAnimatingOut = false;

  public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
    super();
  }

  @Override
  public void onNestedScroll(CoordinatorLayout coordinatorLayout, FabSpeedDial child, View target,
      int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
        dyUnconsumed);

    if (dyConsumed > 0) {
      CoordinatorLayout.LayoutParams layoutParams =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      int fab_bottomMargin = layoutParams.bottomMargin;
      child
          .animate()
          .translationY(child.getHeight() + fab_bottomMargin)
          .setInterpolator(new LinearInterpolator())
          .start();
    } else if (dyConsumed < 0) {
      child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
    }
  }

  @Override
  public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FabSpeedDial child,
      View directTargetChild, View target, int nestedScrollAxes) {
    return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
  }
}*/
