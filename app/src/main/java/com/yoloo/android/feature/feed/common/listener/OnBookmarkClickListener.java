package com.yoloo.android.feature.feed.common.listener;

import android.support.annotation.NonNull;

public interface OnBookmarkClickListener {
  void onBookmarkClick(@NonNull String postId, boolean bookmark);
}
