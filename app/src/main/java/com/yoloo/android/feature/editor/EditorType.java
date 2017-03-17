package com.yoloo.android.feature.editor;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.yoloo.android.feature.editor.EditorType.ASK_QUESTION;
import static com.yoloo.android.feature.editor.EditorType.BLOG;

@IntDef({
    ASK_QUESTION,
    BLOG
})
@Retention(RetentionPolicy.SOURCE)
public @interface EditorType {
  int ASK_QUESTION = 0;
  int BLOG = 1;
}
