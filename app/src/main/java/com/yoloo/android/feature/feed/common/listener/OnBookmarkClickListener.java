package com.yoloo.android.feature.feed.common.listener;

import android.support.annotation.NonNull;
import com.yoloo.android.data.db.PostRealm;

public interface OnBookmarkClickListener {
  void onBookmarkClick(@NonNull PostRealm post, boolean bookmark);
}
