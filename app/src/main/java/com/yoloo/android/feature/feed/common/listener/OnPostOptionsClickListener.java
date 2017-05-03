package com.yoloo.android.feature.feed.common.listener;

import android.view.View;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.data.model.PostRealm;

public interface OnPostOptionsClickListener {
  void onPostOptionsClick(View v, PostRealm post);
}
