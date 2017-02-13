package com.yoloo.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;

public class TextLayoutView extends View {

  private Layout layout;

  public TextLayoutView(Context context) {
    super(context);
  }

  public TextLayoutView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public TextLayoutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setLayout(Layout layout) {
    this.layout = layout;
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.save();

    if (layout != null) {
      canvas.translate(getPaddingLeft(), getPaddingTop());
      layout.draw(canvas);
    }

    canvas.restore();
  }
}
