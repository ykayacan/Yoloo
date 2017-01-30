package com.yoloo.android.feature.feed.common.listener;

import android.view.View;
import com.airbnb.epoxy.EpoxyModel;

public interface OnPostOptionsClickListener {
  void onPostOptionsClick(View v, EpoxyModel<?> model, String postId, String postOwnerId);
}