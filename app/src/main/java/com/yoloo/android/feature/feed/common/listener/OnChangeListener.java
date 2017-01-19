package com.yoloo.android.feature.feed.common.listener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;

public interface OnChangeListener {
  void onChange(@NonNull String itemId, @FeedAction int action, @Nullable Object payload);
}
