/*******************************************************************************
 * Copyright 2016 stfalcon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.yoloo.android.chatkit.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * ImageView with mask what described with Bezier Curves
 */
public class ShapeImageView extends AppCompatImageView {
  private Path path = new Path();

  public ShapeImageView(Context context) {
    super(context);
  }

  public ShapeImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    float halfWidth = (float) w / 2f;
    float firstParam = (float) w * 0.1f;
    float secondParam = (float) w * 0.8875f;

    //Bezier Curves
    path.moveTo(halfWidth, (float) w);
    path.cubicTo(firstParam, (float) w, 0, secondParam, 0, halfWidth);
    path.cubicTo(0, firstParam, firstParam, 0, halfWidth, 0);
    path.cubicTo(secondParam, 0, (float) w, firstParam, (float) w, halfWidth);
    path.cubicTo((float) w, secondParam, secondParam, (float) w, halfWidth, (float) w);
    path.close();
  }

  @Override protected void onDraw(Canvas canvas) {
    if (canvas != null) {
      canvas.clipPath(path);
      super.onDraw(canvas);
    }
  }
}
