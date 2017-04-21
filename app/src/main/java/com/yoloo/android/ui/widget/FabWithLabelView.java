package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import com.yoloo.android.R;

public class FabWithLabelView extends LinearLayout {
  public FabWithLabelView(Context context) {
    super(context);
    init();
  }

  public FabWithLabelView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public FabWithLabelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public FabWithLabelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    setOrientation(LinearLayout.VERTICAL);
    final View view = inflate(getContext(), R.layout.view_fab_with_label, this);
    ButterKnife.bind(this, view);
  }
}
