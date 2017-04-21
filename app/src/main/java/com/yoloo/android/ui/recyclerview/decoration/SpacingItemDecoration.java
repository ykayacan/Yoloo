package com.yoloo.android.ui.recyclerview.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration {

  private int horizontalSpacing;
  private int verticalSpacing;

  public SpacingItemDecoration(int horizontalSpacing, int verticalSpacing) {
    this.horizontalSpacing = horizontalSpacing;
    this.verticalSpacing = verticalSpacing;
  }

  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    outRect.left = this.horizontalSpacing / 2;
    outRect.right = this.horizontalSpacing / 2;
    outRect.top = this.verticalSpacing / 2;
    outRect.bottom = this.verticalSpacing / 2;
  }
}
