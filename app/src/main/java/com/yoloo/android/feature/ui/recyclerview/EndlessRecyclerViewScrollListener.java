package com.yoloo.android.feature.ui.recyclerview;

import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public final class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

  // The total number of items in the dataset after the last load
  private int previousTotal = 0;

  // True if we are still waiting for the last set of data to load.
  private boolean loading = true;

  // The minimum amount of items to have below your current scroll position
  // before loading more.
  private int visibleThreshold = -1;

  private boolean footerVisible;

  private int firstVisibleItem, visibleItemCount, totalItemCount;

  private boolean isOrientationHelperVertical;

  private OrientationHelper orientationHelper;

  private RecyclerView.LayoutManager layoutManager;
  private OnLoadMoreListener onLoadMoreListener;

  public EndlessRecyclerViewScrollListener(RecyclerView.LayoutManager layoutManager,
      OnLoadMoreListener onLoadMoreListener) {
    this.layoutManager = layoutManager;
    this.onLoadMoreListener = onLoadMoreListener;
  }

  private int findFirstVisibleItemPosition(RecyclerView recyclerView) {
    final View child = findOneVisibleChild(0, layoutManager.getChildCount(), false, true);
    return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
  }

  private int findLastVisibleItemPosition(RecyclerView recyclerView) {
    final View child = findOneVisibleChild(recyclerView.getChildCount() - 1, -1, false, true);
    return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
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
    if (dy < 0) {
      return;
    }

    int footerItemCount = 0;

    if (visibleThreshold == -1) {
      visibleThreshold = findLastVisibleItemPosition(recyclerView)
          - findFirstVisibleItemPosition(recyclerView)
          - footerItemCount;
    }

    // Subtract header items.
    visibleItemCount = recyclerView.getChildCount() - footerItemCount - 2;
    totalItemCount = layoutManager.getItemCount() - footerItemCount - 2;
    firstVisibleItem = findFirstVisibleItemPosition(recyclerView);

    // If it's still loading, we check to see if the dataset count has
    // changed, if so we conclude it has finished loading and update the current page
    // number and total item count.
    if (loading && (totalItemCount > previousTotal)) {
      loading = false;
      previousTotal = totalItemCount;
    }

    // If it isn't currently loading, we check to see if we have breached
    // the visibleThreshold and need to reload more data.
    // If we do need to reload some more data, we execute onLoadMore to fetch the data.
    if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
      onLoadMoreListener.onLoadMore();
      loading = true;
    }
  }

  public void setProgressBarVisible(boolean visible) {
    footerVisible = visible;
  }

  public void resetState() {
    previousTotal = 0;
    loading = true;
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

  public interface OnLoadMoreListener {
    void onLoadMore();
  }
}
