package com.yoloo.android.feature.feed.common.listener;

import android.view.View;
import com.airbnb.epoxy.EpoxyModel;

public interface OnProfileClickListener {
  void onProfileClick(View v, EpoxyModel<?> model, String userId);
}
