package com.yoloo.android.feature.feed.common.listener;

import android.view.View;
import com.yoloo.android.data.db.PostRealm;

public interface OnCommentClickListener {
  void onCommentClick(View v, PostRealm post);
}
