package com.yoloo.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.yoloo.android.R;

public class FabGroupView extends LinearLayout {

  @BindView(R.id.fab) FloatingActionButton fab;
  @BindView(R.id.tv_fab_text) TextView tvFabText;

  public FabGroupView(Context context) {
    super(context);
    init(context, null);
  }

  public FabGroupView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public FabGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  public FabGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    setOrientation(LinearLayout.VERTICAL);
    setGravity(Gravity.CENTER);

    View view = inflate(getContext(), R.layout.view_fab_group, this);
    ButterKnife.bind(view);

    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FabGroupView);

    int drawableRes = a.getResourceId(R.styleable.FabGroupView_fabIcon, 0);
    String fabText = a.getString(R.styleable.FabGroupView_fabText);

    a.recycle();

    if (drawableRes != 0) {
      setFabIcon(drawableRes);
    }

    if (!TextUtils.isEmpty(fabText)) {
      tvFabText.setText(fabText);
    }
  }

  public void setText(@StringRes int text) {
    tvFabText.setText(text);
  }

  public void setFabIcon(@DrawableRes int resId) {
    fab.setImageResource(resId);
  }
}
