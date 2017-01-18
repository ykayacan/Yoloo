package com.yoloo.android.feature.category;

import android.support.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.yoloo.android.feature.category.CategoryType.TYPE_DESTINATION;
import static com.yoloo.android.feature.category.CategoryType.TYPE_THEME;

@StringDef({TYPE_THEME, TYPE_DESTINATION})
@Retention(RetentionPolicy.SOURCE)
public @interface CategoryType {
  String TYPE_THEME = "THEME";
  String TYPE_DESTINATION = "DESTINATION";
}
