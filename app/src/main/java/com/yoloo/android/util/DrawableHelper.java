package com.yoloo.android.util;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public final class DrawableHelper {

  private Context context;

  @ColorInt
  private int color;

  private Drawable drawable;
  private Drawable wrappedDrawable;

  private DrawableHelper(@NonNull Context context) {
    this.context = context;
  }

  public static DrawableHelper withContext(@NonNull Context context) {
    return new DrawableHelper(context);
  }

  public DrawableHelper withDrawable(@DrawableRes int drawableRes) {
    drawable = AppCompatResources.getDrawable(context, drawableRes);
    return this;
  }

  public DrawableHelper withDrawable(@NonNull Drawable drawable) {
    this.drawable = drawable;
    return this;
  }

  public DrawableHelper withColor(@ColorRes int colorRes) {
    color = ContextCompat.getColor(context, colorRes);
    return this;
  }

  public DrawableHelper tint() {
    if (drawable == null) {
      throw new NullPointerException("Drawable is empty!");
    }

    if (color == 0) {
      throw new IllegalStateException("Color cannot be 0!");
    }

    wrappedDrawable = drawable.mutate();
    wrappedDrawable = DrawableCompat.wrap(wrappedDrawable);
    DrawableCompat.setTint(wrappedDrawable, color);
    DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN);

    return this;
  }

  public void applyTo(@NonNull ImageView imageView) {
    if (wrappedDrawable == null) {
      throw new NullPointerException("É preciso chamar o método tint()");
    }

    imageView.setImageDrawable(wrappedDrawable);
  }

  public void applyTo(@NonNull MenuItem menuItem) {
    Drawable drawable = menuItem.getIcon();
    if (drawable != null) {
      drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
  }

  public void applyTo(@NonNull Menu menu) {
    final int size = menu.size();
    for (int i = 0; i < size; i++) {
      applyTo(menu.getItem(i));
    }
  }

  public Drawable get() {
    if (wrappedDrawable == null) {
      throw new NullPointerException("É preciso chamar o método tint()");
    }

    return wrappedDrawable;
  }
}
