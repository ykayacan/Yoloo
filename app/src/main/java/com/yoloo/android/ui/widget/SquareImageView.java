package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class SquareImageView extends AppCompatImageView {
  public SquareImageView(Context context) {
    super(context);
  }

  public SquareImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    // If one from the measures is match_parent, use that one to determine the size.
    // If not, use the default implementation from onMeasure.
    if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
      setMeasuredDimension(widthSize, widthSize);
    } else if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
      setMeasuredDimension(heightSize, heightSize);
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }
}
