package com.yoloo.android.feature.comment;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.yoloo.android.feature.comment.PostType.TYPE_BLOG;
import static com.yoloo.android.feature.comment.PostType.TYPE_NORMAL;
import static com.yoloo.android.feature.comment.PostType.TYPE_RICH;

@IntDef({
    TYPE_NORMAL,
    TYPE_RICH,
    TYPE_BLOG
})
@Retention(RetentionPolicy.SOURCE)
public @interface PostType {
  int TYPE_NORMAL = 1;
  int TYPE_RICH = 2;
  int TYPE_BLOG = 3;
}
