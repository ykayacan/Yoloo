package com.yoloo.android.feature.feed.common.listener;

import android.view.View;

public interface OnCommentClickListener {
  void onCommentClick(View v, String itemId, String acceptedCommentId);
}
