package com.yoloo.android.ui.recyclerview;

import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

  // The total number getPost items in the dataset after the last load
  private int previousTotal = 0;

  // True if we are still waiting for the last set getPost data to load.
  private boolean loading = true;

  // The minimum amount getPost items to have below your current scroll position
  // before loading more.
  private int visibleThreshold = -1;

  private int firstVisibleItem, visibleItemCount, totalItemCount;

  private boolean isOrientationHelperVertical;
  private OrientationHelper orientationHelper;

  private RecyclerView.LayoutManager layoutManager;

  public EndlessRecyclerOnScrollListener() {
  }

  public EndlessRecyclerOnScrollListener(RecyclerView.LayoutManager layoutManager) {
    this.layoutManager = layoutManager;
  }

  public EndlessRecyclerOnScrollListener(int visibleThreshold) {
    this.visibleThreshold = visibleThreshold;
  }

  public EndlessRecyclerOnScrollListener(RecyclerView.LayoutManager layoutManager,
      int visibleThreshold) {
    this.layoutManager = layoutManager;
    this.visibleThreshold = visibleThreshold;
  }

  private int findFirstVisibleItemPosition(RecyclerView recyclerView) {
    final View child = findOneVisibleChild(0, layoutManager.getChildCount(), false, true);
    return recyclerView.getChildAdapterPosition(child);
  }

  private int findLastVisibleItemPosition(RecyclerView recyclerView) {
    final View child = findOneVisibleChild(recyclerView.getChildCount() - 1, -1, false, true);
    return recyclerView.getChildAdapterPosition(child);
  }

  private View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
      boolean acceptPartiallyVisible) {
    if (layoutManager.canScrollVertically() != isOrientationHelperVertical
        || orientationHelper == null) {
      isOrientationHelperVertical = layoutManager.canScrollVertically();
      orientationHelper =
          isOrientationHelperVertical ? OrientationHelper.createVerticalHelper(layoutManager)
              : OrientationHelper.createHorizontalHelper(layoutManager);
    }

    final int start = orientationHelper.getStartAfterPadding();
    final int end = orientationHelper.getEndAfterPadding();
    final int next = toIndex > fromIndex ? 1 : -1;

    View partiallyVisible = null;

    for (int i = fromIndex; i != toIndex; i += next) {
      final View child = layoutManager.getChildAt(i);

      if (child != null) {
        final int childStart = orientationHelper.getDecoratedStart(child);
        final int childEnd = orientationHelper.getDecoratedEnd(child);
        if (childStart < end && childEnd > start) {
          if (completelyVisible) {
            if (childStart >= start && childEnd <= end) {
              return child;
            } else if (acceptPartiallyVisible && partiallyVisible == null) {
              partiallyVisible = child;
            }
          } else {
            return child;
          }
        }
      }
    }
    return partiallyVisible;
  }

  @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);
    if (layoutManager == null) {
      layoutManager = recyclerView.getLayoutManager();
    }

    // bail out if scrolling upward or already loading data
    if (dy <= 0) {
      return;
    }

    if (visibleThreshold == -1) {
      visibleThreshold = findLastVisibleItemPosition(recyclerView)
          - findFirstVisibleItemPosition(recyclerView);
    }

    visibleItemCount = recyclerView.getChildCount();
    totalItemCount = layoutManager.getItemCount();
    firstVisibleItem = findFirstVisibleItemPosition(recyclerView);

    // If the total item count is zero and the previous isn't, assume the
    // list is invalidated and should be reset back to initial state
    if (totalItemCount < previousTotal) {
      previousTotal = 0;
      loading = true;
    }

    // If it's still loading, we check to see if the dataset count has
    // changed, if so we conclude it has finished loading and updatePost the current page
    // number and total item count.
    if (loading && (totalItemCount > previousTotal)) {
      loading = false;
      previousTotal = totalItemCount;
    }

    // If it isn't currently loading, we check to see if we have breached
    // the visibleThreshold and need to reload more data.
    // If we do need to reload some more data, we execute onLoadMore to fetch the data.
    if (!loading && (firstVisibleItem + visibleItemCount + visibleThreshold) >= totalItemCount) {
      onLoadMore();
      loading = true;
    }
  }

  public RecyclerView.LayoutManager getLayoutManager() {
    return layoutManager;
  }

  public int getTotalItemCount() {
    return totalItemCount;
  }

  public int getFirstVisibleItem() {
    return firstVisibleItem;
  }

  public int getVisibleItemCount() {
    return visibleItemCount;
  }

  public abstract void onLoadMore();
}
