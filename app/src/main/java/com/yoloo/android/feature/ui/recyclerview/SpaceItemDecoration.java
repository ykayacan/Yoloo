package com.yoloo.android.feature.ui.recyclerview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import com.yoloo.android.util.DisplayUtil;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

  public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
  public static final int VERTICAL = LinearLayout.VERTICAL;

  private final int space;

  /**
   * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}.
   */
  private int orientation;

  public SpaceItemDecoration(int space, int orientation) {
    this.space = space;
    setOrientation(orientation);
  }

  /**
   * Sets the orientation for this divider. This should be called if {@link
   * RecyclerView.LayoutManager} changes orientation.
   *
   * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
   */
  public void setOrientation(int orientation) {
    if (orientation != HORIZONTAL && orientation != VERTICAL) {
      throw new IllegalArgumentException(
          "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
    }
    this.orientation = orientation;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    if (orientation == VERTICAL) {
      outRect.top = DisplayUtil.dpToPx(space);
    } else {
      outRect.right = DisplayUtil.dpToPx(space);
    }
  }
}
