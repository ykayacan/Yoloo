/*
 * Copyright (c) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.yoloo.android.feature.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.yoloo.android.R;
import com.yoloo.android.util.DisplayUtil;
import timber.log.Timber;

/**
 * An extension to {@link ImageView} that draws a play button over the main image applies a tint
 * to the image when it is marked as played.
 */
public class PhotoThumbnail extends AppCompatImageView {

  private Drawable closeIcon;

  private int mPlayedTint;

  public PhotoThumbnail(Context context) {
    this(context, null, 0);
  }

  public PhotoThumbnail(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PhotoThumbnail(final Context context, final AttributeSet attrs,
      final int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    final TypedArray a = getContext().obtainStyledAttributes(
        attrs, R.styleable.VideoThumbnail, defStyleAttr, 0);
    closeIcon = a.getDrawable(R.styleable.VideoThumbnail_playIcon);
    mPlayedTint = a.getColor(R.styleable.VideoThumbnail_playedTint, Color.TRANSPARENT);
    a.recycle();
    setScaleType(ScaleType.CENTER_CROP);
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    final int width = DisplayUtil.dpToPx(90);
    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
    // Place the close icon in the center of this view
    if (closeIcon != null) {
      final int closeLeft =
          (getMeasuredWidth() - closeIcon.getIntrinsicWidth()) - DisplayUtil.dpToPx(8);
      final int closeTop = DisplayUtil.dpToPx(8);
      closeIcon.setBounds(closeLeft, closeTop,
          closeLeft + closeIcon.getIntrinsicWidth(),
          closeTop + closeIcon.getIntrinsicHeight());
    }
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);
    if (closeIcon != null) {
      closeIcon.draw(canvas);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      if (event.getRawX() >= getRight() - getPaddingRight()) {
        Timber.d("Yesss");
      }
    }
    return super.onTouchEvent(event);
  }

  public void setCloseIcon(@DrawableRes int closeIconRes) {
    this.closeIcon = AppCompatResources.getDrawable(getContext(), closeIconRes);
  }
}
