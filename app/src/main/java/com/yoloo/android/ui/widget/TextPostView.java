package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import com.yoloo.android.R;

public class TextPostView extends ConstraintLayout {

  public TextPostView(Context context) {
    super(context);
    init(context);
  }

  public TextPostView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public TextPostView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    inflate(context, R.layout.item_feed_question_text,this);
  }
}
