package com.yoloo.android.util;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

/**
 * Helper for working with drawables
 */
public final class DrawableHelper {

  private static final int NO_COLOR = -1;

  private int tintColor = NO_COLOR;

  private Drawable drawable;
  private Drawable wrappedDrawable;

  private DrawableHelper() {
    // empty constructor
  }

  public static DrawableHelper create() {
    return new DrawableHelper();
  }

  public DrawableHelper withDrawable(Context context, @DrawableRes int drawableRes) {
    withDrawable(AppCompatResources.getDrawable(context, drawableRes));
    return this;
  }

  public DrawableHelper withDrawable(@NonNull Drawable drawable) {
    this.drawable = drawable;
    return this;
  }

  public DrawableHelper withColor(Context context, @ColorRes int colorRes) {
    withColor(ContextCompat.getColor(context, colorRes));
    return this;
  }

  public DrawableHelper withColor(@ColorInt int color) {
    this.tintColor = color;
    return this;
  }

  public DrawableHelper tint() {
    tint(PorterDuff.Mode.SRC_IN);
    return this;
  }

  public DrawableHelper tint(@Nullable PorterDuff.Mode mode) {
    wrappedDrawable = drawable.mutate();
    wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
    DrawableCompat.setTint(wrappedDrawable, tintColor);
    DrawableCompat.setTintMode(wrappedDrawable, mode);
    return this;
  }

  public void applyTo(@NonNull ImageView imageView) {
    imageView.setImageDrawable(wrappedDrawable);
  }

  public void applyTo(@NonNull Menu menu) {
    final int size = menu.size();
    for (int i = 0; i < size; i++) {
      applyTo(menu.getItem(i));
    }
  }

  public void applyTo(@NonNull MenuItem menuItem) {
    Drawable drawable = menuItem.getIcon();
    if (drawable != null) {
      drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
    }
  }
}
