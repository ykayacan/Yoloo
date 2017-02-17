package com.yoloo.android.util.glide.transfromation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

public class CropCircleTransformation implements Transformation<Bitmap> {

  private final BitmapPool bitmapPool;

  public CropCircleTransformation(Context context) {
    bitmapPool = Glide.get(context).getBitmapPool();
  }

  @Override
  public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
    final Bitmap source = resource.get();
    final int size = Math.min(source.getWidth(), source.getHeight());

    final int width = (source.getWidth() - size) / 2;
    final int height = (source.getHeight() - size) / 2;

    Bitmap bitmap = bitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
    if (bitmap == null) {
      bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    }

    final Canvas canvas = new Canvas(bitmap);
    final Paint paint = new Paint();
    final BitmapShader shader =
        new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
    if (width != 0 || height != 0) {
      // source isn't square, move viewport to center
      final Matrix matrix = new Matrix();
      matrix.setTranslate(-width, -height);
      shader.setLocalMatrix(matrix);
    }
    paint.setShader(shader);
    paint.setAntiAlias(true);

    final float r = size / 2f;

    canvas.drawCircle(r, r, r, paint);

    return BitmapResource.obtain(bitmap, bitmapPool);
  }

  @Override
  public String getId() {
    return CropCircleTransformation.class.getName();
  }
}
