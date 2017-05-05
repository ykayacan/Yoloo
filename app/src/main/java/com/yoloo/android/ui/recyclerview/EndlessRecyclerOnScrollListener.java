package com.yoloo.android.ui.recyclerview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

  // The minimum amount of items to have below your current scroll position
  // before loading more.
  private int visibleThreshold = 5;

  // The current offset index of data you have loaded
  private int currentPage = 0;

  // The total number of items in the dataset after the last load
  private int previousTotalItemCount = 0;

  // True if we are still waiting for the last set of data to load.
  private boolean loading = true;

  // Sets the starting page index
  private int startingPageIndex = 0;

  private RecyclerView.LayoutManager layoutManager;

  public EndlessRecyclerOnScrollListener(LinearLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
  }

  public EndlessRecyclerOnScrollListener(GridLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
    visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
  }

  public EndlessRecyclerOnScrollListener(StaggeredGridLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
    visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
  }

  private int getLastVisibleItem(int[] lastVisibleItemPositions) {
    int maxSize = 0;
    final int size = lastVisibleItemPositions.length;
    for (int i = 0; i < size; i++) {
      if (i == 0) {
        maxSize = lastVisibleItemPositions[i];
      } else if (lastVisibleItemPositions[i] > maxSize) {
        maxSize = lastVisibleItemPositions[i];
      }
    }
    return maxSize;
  }

  // This happens many times a second during a scroll, so be wary of the code you place here.
  // We are given a few useful parameters to help us work out if we need to load some more data,
  // but first we check if we are waiting for the previous load to finish.
  @Override
  public void onScrolled(RecyclerView view, int dx, int dy) {
    int lastVisibleItemPosition = 0;
    int totalItemCount = layoutManager.getItemCount();

    if (layoutManager instanceof StaggeredGridLayoutManager) {
      int[] lastVisibleItemPositions =
          ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
      // get maximum element within the list
      lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
    } else if (layoutManager instanceof GridLayoutManager) {
      lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
    } else if (layoutManager instanceof LinearLayoutManager) {
      lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
    }

    // If it’s still loading, we check to see if the dataset count has
    // changed, if so we conclude it has finished loading and update the current page
    // number and total item count.
    if (loading && (totalItemCount > previousTotalItemCount)) {
      loading = false;
      previousTotalItemCount = totalItemCount;
    }

    // If it isn’t currently loading, we check to see if we have breached
    // the visibleThreshold and need to reload more data.
    // If we do need to reload some more data, we execute onLoadMore to fetch the data.
    // threshold should reflect how many total columns there are too
    if (!loading
        && (lastVisibleItemPosition + visibleThreshold) > totalItemCount
        && view.getAdapter().getItemCount()
        > visibleThreshold) {// This condition will useful when recyclerview has less than visibleThreshold items
      currentPage++;
      onLoadMore(currentPage, totalItemCount, view);
      loading = true;
    }
  }

  // Call whenever performing new searches
  public void resetState() {
    this.currentPage = startingPageIndex;
    this.previousTotalItemCount = 0;
    this.loading = true;
  }

  // Defines the process for actually loading more data based on page
  public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);
}
