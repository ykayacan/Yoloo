package com.yoloo.android.ui.widget.delegate;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.widget.TextView;
import com.yoloo.android.R;

public class TextViewVectorSupportDelegate {

  public void init(TextView textView, Context context, AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CompatTextView);

    // Obtain DrawableManager used to pull Drawables safely, and check if we're in RTL
    final boolean rtl = ViewCompat.getLayoutDirection(textView) == ViewCompat.LAYOUT_DIRECTION_RTL;

    // Grab the compat drawable resources from the XML
    int startDrawableRes = a.getResourceId(R.styleable.CompatTextView_drawableStart, 0);
    int topDrawableRes = a.getResourceId(R.styleable.CompatTextView_drawableTop, 0);
    int endDrawableRes = a.getResourceId(R.styleable.CompatTextView_drawableEnd, 0);
    int bottomDrawableRes = a.getResourceId(R.styleable.CompatTextView_drawableBottom, 0);

    a.recycle();

    // Load the used drawables, falling back to whatever may be set in
    // an "android:" namespace attribute
    Drawable[] currentDrawables = textView.getCompoundDrawables();

    Drawable left = getDrawable(context, startDrawableRes, currentDrawables, 0);
    Drawable top = getDrawable(context, topDrawableRes, currentDrawables, 1);
    Drawable right = getDrawable(context, endDrawableRes, currentDrawables, 2);
    Drawable bottom = getDrawable(context, bottomDrawableRes, currentDrawables, 3);

    // Account for RTL and apply the compound Drawables
    Drawable start = rtl ? right : left;
    Drawable end = rtl ? left : right;
    textView.setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom);
  }

  private Drawable getDrawable(Context context, int drawableRes, Drawable[] drawables, int index) {
    return drawableRes != 0
        ? AppCompatResources.getDrawable(context, drawableRes)
        : drawables[index];
  }
}
