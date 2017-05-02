package com.yoloo.android.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.yoloo.android.R;
import com.yoloo.android.util.DisplayUtil;

public class BountyView extends LinearLayout {

  @BindView(R.id.tv_bounty_title) TextView tvTitle;
  @BindView(R.id.tv_bounty_content) TextView tvContent;
  @BindView(R.id.tv_bounty_price) TextView tvPrice;

  public BountyView(Context context) {
    super(context);
    init(context, null);
  }

  public BountyView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public BountyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @TargetApi(21)
  public BountyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    inflate(getContext(), R.layout.layout_bounty, this);
    ButterKnife.bind(this);

    setOrientation(VERTICAL);

    int padding16 = DisplayUtil.dpToPx(16);
    int padding24 = DisplayUtil.dpToPx(24);
    setPadding(padding24, padding16, padding24, padding16);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BountyView);

    String bountyTitle = a.getString(R.styleable.BountyView_bountyTitle);
    int bountyDrawableRes = a.getResourceId(R.styleable.BountyView_bountyDrawable, 0);
    String bountyPrice = a.getString(R.styleable.BountyView_bountyPrice);

    if (!TextUtils.isEmpty(bountyTitle)) {
      tvTitle.setText(bountyTitle);
    }

    if (bountyDrawableRes != 0) {
      Drawable drawable = AppCompatResources.getDrawable(context, bountyDrawableRes);
      tvContent.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    if (!TextUtils.isEmpty(bountyPrice)) {
      tvPrice.setText(bountyPrice);
    }

    a.recycle();
  }

  public int getBountyValue() {
    return Integer.valueOf(tvContent.getText().toString());
  }
}
