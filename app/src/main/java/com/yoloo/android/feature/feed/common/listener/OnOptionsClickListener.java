package com.yoloo.android.feature.feed.common.listener;

import android.view.View;
import com.airbnb.epoxy.EpoxyModel;

public interface OnOptionsClickListener {
  void onOptionsClick(View v, EpoxyModel<?> model, String postId, String postOwnerId);
}