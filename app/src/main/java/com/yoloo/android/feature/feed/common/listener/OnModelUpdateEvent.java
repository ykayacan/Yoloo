package com.yoloo.android.feature.feed.common.listener;

import android.support.annotation.Nullable;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;

public interface OnModelUpdateEvent {

  void onModelUpdateEvent(@FeedAction int action, @Nullable Object payload);
}
