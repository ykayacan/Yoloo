package com.yoloo.android.feature.feed.common.model;

import android.widget.FrameLayout;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;

public class LoadingModel extends EpoxyModel<FrameLayout> {

  @Override protected int getDefaultLayout() {
    return R.layout.item_feed_loading;
  }
}
