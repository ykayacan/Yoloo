package com.yoloo.android.util.glide;

/*
  Copyright (C) 2015 Wasabeef

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

public class RoundedCornersTransformation implements Transformation<Bitmap> {

  private static RoundedCornersTransformation instance;

  private BitmapPool bitmapPool;
  private int radius;
  private int diameter;
  private int margin;
  private CornerType cornerType;

  private RoundedCornersTransformation(Context context, int radius, int margin) {
    this(context, radius, margin, CornerType.ALL);
  }

  private RoundedCornersTransformation(BitmapPool pool, int radius, int margin) {
    this(pool, radius, margin, CornerType.ALL);
  }

  private RoundedCornersTransformation(Context context, int radius, int margin,
      CornerType cornerType) {
    this(Glide.get(context).getBitmapPool(), radius, margin, cornerType);
  }

  private RoundedCornersTransformation(BitmapPool pool, int radius, int margin,
      CornerType cornerType) {
    bitmapPool = pool;
    this.radius = radius;
    diameter = this.radius * 2;
    this.margin = margin;
    this.cornerType = cornerType;
  }

  public static RoundedCornersTransformation getInstance(Context context, int radius, int margin) {
    if (instance == null) {
      instance = new RoundedCornersTransformation(context, radius, margin);
    }
    return instance;
  }

  @Override
  public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
    final Bitmap source = resource.get();

    final int width = source.getWidth();
    final int height = source.getHeight();

    Bitmap bitmap = bitmapPool.get(width, height, Bitmap.Config.ARGB_8888);
    if (bitmap == null) {
      bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    final Canvas canvas = new Canvas(bitmap);
    final Paint paint = new Paint();

    paint.setAntiAlias(true);
    paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
    drawRoundRect(canvas, paint, width, height);
    return BitmapResource.obtain(bitmap, bitmapPool);
  }

  private void drawRoundRect(Canvas canvas, Paint paint, float width, float height) {
    float right = width - margin;
    float bottom = height - margin;

    switch (cornerType) {
      case ALL:
        canvas.drawRoundRect(new RectF(margin, margin, right, bottom), radius, radius, paint);
        break;
      case TOP_LEFT:
        drawTopLeftRoundRect(canvas, paint, right, bottom);
        break;
      case TOP_RIGHT:
        drawTopRightRoundRect(canvas, paint, right, bottom);
        break;
      case BOTTOM_LEFT:
        drawBottomLeftRoundRect(canvas, paint, right, bottom);
        break;
      case BOTTOM_RIGHT:
        drawBottomRightRoundRect(canvas, paint, right, bottom);
        break;
      case TOP:
        drawTopRoundRect(canvas, paint, right, bottom);
        break;
      case BOTTOM:
        drawBottomRoundRect(canvas, paint, right, bottom);
        break;
      case LEFT:
        drawLeftRoundRect(canvas, paint, right, bottom);
        break;
      case RIGHT:
        drawRightRoundRect(canvas, paint, right, bottom);
        break;
      case OTHER_TOP_LEFT:
        drawOtherTopLeftRoundRect(canvas, paint, right, bottom);
        break;
      case OTHER_TOP_RIGHT:
        drawOtherTopRightRoundRect(canvas, paint, right, bottom);
        break;
      case OTHER_BOTTOM_LEFT:
        drawOtherBottomLeftRoundRect(canvas, paint, right, bottom);
        break;
      case OTHER_BOTTOM_RIGHT:
        drawOtherBottomRightRoundRect(canvas, paint, right, bottom);
        break;
      case DIAGONAL_FROM_TOP_LEFT:
        drawDiagonalFromTopLeftRoundRect(canvas, paint, right, bottom);
        break;
      case DIAGONAL_FROM_TOP_RIGHT:
        drawDiagonalFromTopRightRoundRect(canvas, paint, right, bottom);
        break;
      default:
        canvas.drawRoundRect(new RectF(margin, margin, right, bottom), radius, radius, paint);
        break;
    }
  }

  private void drawTopLeftRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(margin, margin, margin + diameter, margin + diameter),
        radius, radius, paint);
    canvas.drawRect(new RectF(margin, margin + radius, margin + radius, bottom), paint);
    canvas.drawRect(new RectF(margin + radius, margin, right, bottom), paint);
  }

  private void drawTopRightRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(right - diameter, margin, right, margin + diameter), radius,
        radius, paint);
    canvas.drawRect(new RectF(margin, margin, right - radius, bottom), paint);
    canvas.drawRect(new RectF(right - radius, margin + radius, right, bottom), paint);
  }

  private void drawBottomLeftRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(margin, bottom - diameter, margin + diameter, bottom),
        radius, radius, paint);
    canvas.drawRect(new RectF(margin, margin, margin + diameter, bottom - radius), paint);
    canvas.drawRect(new RectF(margin + radius, margin, right, bottom), paint);
  }

  private void drawBottomRightRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(right - diameter, bottom - diameter, right, bottom), radius,
        radius, paint);
    canvas.drawRect(new RectF(margin, margin, right - radius, bottom), paint);
    canvas.drawRect(new RectF(right - radius, margin, right, bottom - radius), paint);
  }

  private void drawTopRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(margin, margin, right, margin + diameter), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin, margin + radius, right, bottom), paint);
  }

  private void drawBottomRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(margin, bottom - diameter, right, bottom), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin, margin, right, bottom - radius), paint);
  }

  private void drawLeftRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(margin, margin, margin + diameter, bottom), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin + radius, margin, right, bottom), paint);
  }

  private void drawRightRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(right - diameter, margin, right, bottom), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin, margin, right - radius, bottom), paint);
  }

  private void drawOtherTopLeftRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(margin, bottom - diameter, right, bottom), radius, radius,
        paint);
    canvas.drawRoundRect(new RectF(right - diameter, margin, right, bottom), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin, margin, right - radius, bottom - radius), paint);
  }

  private void drawOtherTopRightRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(margin, margin, margin + diameter, bottom), radius, radius,
        paint);
    canvas.drawRoundRect(new RectF(margin, bottom - diameter, right, bottom), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin + radius, margin, right, bottom - radius), paint);
  }

  private void drawOtherBottomLeftRoundRect(Canvas canvas, Paint paint, float right, float bottom) {
    canvas.drawRoundRect(new RectF(margin, margin, right, margin + diameter), radius, radius,
        paint);
    canvas.drawRoundRect(new RectF(right - diameter, margin, right, bottom), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin, margin + radius, right - radius, bottom), paint);
  }

  private void drawOtherBottomRightRoundRect(Canvas canvas, Paint paint, float right,
      float bottom) {
    canvas.drawRoundRect(new RectF(margin, margin, right, margin + diameter), radius, radius,
        paint);
    canvas.drawRoundRect(new RectF(margin, margin, margin + diameter, bottom), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin + radius, margin + radius, right, bottom), paint);
  }

  private void drawDiagonalFromTopLeftRoundRect(Canvas canvas, Paint paint, float right,
      float bottom) {
    canvas.drawRoundRect(new RectF(margin, margin, margin + diameter, margin + diameter),
        radius, radius, paint);
    canvas.drawRoundRect(new RectF(right - diameter, bottom - diameter, right, bottom), radius,
        radius, paint);
    canvas.drawRect(new RectF(margin, margin + radius, right - diameter, bottom), paint);
    canvas.drawRect(new RectF(margin + diameter, margin, right, bottom - radius), paint);
  }

  private void drawDiagonalFromTopRightRoundRect(Canvas canvas, Paint paint, float right,
      float bottom) {
    canvas.drawRoundRect(new RectF(right - diameter, margin, right, margin + diameter), radius,
        radius, paint);
    canvas.drawRoundRect(new RectF(margin, bottom - diameter, margin + diameter, bottom),
        radius, radius, paint);
    canvas.drawRect(new RectF(margin, margin, right - radius, bottom - radius), paint);
    canvas.drawRect(new RectF(margin + radius, margin + radius, right, bottom), paint);
  }

  @Override
  public String getId() {
    return "RoundedTransformation(radius=" + radius + ", margin=" + margin + ", diameter="
        + diameter + ", cornerType=" + cornerType.name() + ")";
  }

  public enum CornerType {
    ALL,
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    TOP, BOTTOM, LEFT, RIGHT,
    OTHER_TOP_LEFT, OTHER_TOP_RIGHT, OTHER_BOTTOM_LEFT, OTHER_BOTTOM_RIGHT,
    DIAGONAL_FROM_TOP_LEFT, DIAGONAL_FROM_TOP_RIGHT
  }
}
