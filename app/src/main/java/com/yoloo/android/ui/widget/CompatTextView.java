package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import com.yoloo.android.ui.widget.delegate.TextViewVectorSupportDelegate;

public class CompatTextView extends AppCompatTextView {

  private TextViewVectorSupportDelegate textViewVectorSupportDelegate;

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
    getTextViewVectorSupportDelegate().init(this, context, attrs);
  }

  private TextViewVectorSupportDelegate getTextViewVectorSupportDelegate() {
    if (textViewVectorSupportDelegate == null) {
      textViewVectorSupportDelegate = new TextViewVectorSupportDelegate();
    }
    return textViewVectorSupportDelegate;
  }
}
