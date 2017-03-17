package com.yoloo.android.feature.feed.common.listener;

import android.view.View;
import com.yoloo.android.data.model.PostRealm;

public interface OnReadMoreClickListener {
  void onReadMoreClick(View v, PostRealm post);
}
