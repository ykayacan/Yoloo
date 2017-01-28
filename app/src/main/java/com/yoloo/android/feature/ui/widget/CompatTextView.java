package com.yoloo.android.feature.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import com.yoloo.android.R;

public class CompatTextView extends AppCompatTextView {

  public CompatTextView(Context context) {
    super(context);
    init(context, null);
  }

  public CompatTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public CompatTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    if (attrs != null) {

      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CompatTextView);

      // Obtain DrawableManager used to pull Drawables safely, and check if we're in RTL
      boolean rtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

      // Grab the compat drawable resources from the XML
      int startDrawableRes = a.getResourceId(R.styleable.CompatTextView_drawableStart, 0);
      int topDrawableRes = a.getResourceId(R.styleable.CompatTextView_drawableTop, 0);
      int endDrawableRes = a.getResourceId(R.styleable.CompatTextView_drawableEnd, 0);
      int bottomDrawableRes = a.getResourceId(R.styleable.CompatTextView_drawableBottom, 0);

      a.recycle();

      // Load the used drawables, falling back to whatever may be set in an "android:" namespace attribute
      Drawable[] currentDrawables = getCompoundDrawables();
      Drawable left =
          startDrawableRes != 0 ? AppCompatResources.getDrawable(context, startDrawableRes)
              : currentDrawables[0];
      Drawable top =
          topDrawableRes != 0 ? AppCompatResources.getDrawable(context, topDrawableRes)
              : currentDrawables[1];
      Drawable right =
          endDrawableRes != 0 ? AppCompatResources.getDrawable(context, endDrawableRes)
              : currentDrawables[2];
      Drawable bottom =
          bottomDrawableRes != 0 ? AppCompatResources.getDrawable(context, bottomDrawableRes)
              : currentDrawables[3];

      // Account for RTL and apply the compound Drawables
      Drawable start = rtl ? right : left;
      Drawable end = rtl ? left : right;
      setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom);
    }
  }
}