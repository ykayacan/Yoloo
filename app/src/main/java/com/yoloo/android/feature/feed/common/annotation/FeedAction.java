package com.yoloo.android.feature.feed.common.annotation;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
    FeedAction.UNSPECIFIED,
    FeedAction.UPDATE,
    FeedAction.DELETE
})
@Retention(RetentionPolicy.SOURCE)
public @interface FeedAction {
  int UNSPECIFIED = -1;
  int UPDATE = 0;
  int DELETE = 1;
}
