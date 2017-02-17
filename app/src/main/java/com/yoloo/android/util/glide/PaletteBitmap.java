package com.yoloo.android.util.glide;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

/**
 * A simple wrapper for a {@link Palette} and a {@link Bitmap}.
 */
public class PaletteBitmap {
  public final Palette palette;
  public final Bitmap bitmap;

  public PaletteBitmap(@NonNull Bitmap bitmap, Palette palette) {
    this.bitmap = bitmap;
    this.palette = palette;
  }
}
