package com.yoloo.android.feature.feed.common.annotation;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.yoloo.android.feature.feed.common.annotation.PostType.TYPE_BLOG;
import static com.yoloo.android.feature.feed.common.annotation.PostType.TYPE_NORMAL;
import static com.yoloo.android.feature.feed.common.annotation.PostType.TYPE_RICH;

@IntDef({
    TYPE_NORMAL,
    TYPE_RICH,
    TYPE_BLOG
})
@Retention(RetentionPolicy.SOURCE)
public @interface PostType {
  int TYPE_NORMAL = 0;
  int TYPE_RICH = 1;
  int TYPE_BLOG = 2;
}
