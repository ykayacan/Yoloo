package com.yoloo.android.ui.recyclerview;

import android.support.annotation.CallSuper;
import android.view.View;
import butterknife.ButterKnife;
import com.airbnb.epoxy.EpoxyHolder;

public class BaseEpoxyHolder extends EpoxyHolder {

  public View itemView;

  @CallSuper @Override protected void bindView(View itemView) {
    this.itemView = itemView;
    ButterKnife.bind(this, itemView);
  }
}
