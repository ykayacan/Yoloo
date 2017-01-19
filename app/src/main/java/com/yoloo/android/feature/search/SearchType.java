package com.yoloo.android.feature.search;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
    SearchType.TAG,
    SearchType.USER
})
@Retention(RetentionPolicy.SOURCE)
public @interface SearchType {
  int TAG = 0;
  int USER = 1;
}
