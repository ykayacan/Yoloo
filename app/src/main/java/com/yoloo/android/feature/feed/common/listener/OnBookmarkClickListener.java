package com.yoloo.android.feature.feed.common.listener;

import android.support.annotation.NonNull;
import com.yoloo.android.data.model.PostRealm;

public interface OnBookmarkClickListener {
  void onBookmarkClick(@NonNull PostRealm post, boolean bookmark);
}
