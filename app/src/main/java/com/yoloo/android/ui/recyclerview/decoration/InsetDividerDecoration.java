/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yoloo.android.ui.recyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * A decoration which draws a horizontal divider between {@link RecyclerView.ViewHolder}s from a
 * given type; with a left inset.
 */
public class InsetDividerDecoration extends RecyclerView.ItemDecoration {

  private final int layoutRes;
  private final Paint paint;
  private final int inset;
  private final int height;

  public InsetDividerDecoration(@LayoutRes int dividedLayoutRes, int dividerHeight, int leftInset,
      @ColorInt int dividerColor) {
    layoutRes = dividedLayoutRes;
    inset = leftInset;
    height = dividerHeight;
    paint = new Paint();
    paint.setColor(dividerColor);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(dividerHeight);
  }

  @Override
  public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
    final int childCount = parent.getChildCount();
    if (childCount < 2) {
      return;
    }

    final RecyclerView.LayoutManager lm = parent.getLayoutManager();
    float[] lines = new float[childCount * 4];
    boolean hasDividers = false;

    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      final RecyclerView.ViewHolder holder = parent.getChildViewHolder(child);
      final int viewType = holder.getItemViewType();

      if (viewType == layoutRes) {
        // skip if this *or next* view is activated
        if (child.isActivated() || (i + 1 < childCount && parent.getChildAt(i + 1).isActivated())) {
          continue;
        }
        lines[i * 4] = inset + lm.getDecoratedLeft(child);
        lines[(i * 4) + 2] = lm.getDecoratedRight(child);
        final int y = lm.getDecoratedBottom(child) + (int) child.getTranslationY() - height;
        lines[(i * 4) + 1] = y;
        lines[(i * 4) + 3] = y;
        hasDividers = true;
      }
    }
    if (hasDividers) {
      canvas.drawLines(lines, paint);
    }
  }
}
