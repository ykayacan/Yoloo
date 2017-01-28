package com.yoloo.android.feature.write;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.yoloo.android.feature.write.EditorType.ASK_QUESTION;
import static com.yoloo.android.feature.write.EditorType.SHARE_TRIP;

@IntDef({
    ASK_QUESTION,
    SHARE_TRIP
})
@Retention(RetentionPolicy.SOURCE)
public @interface EditorType {
  int ASK_QUESTION = 0;
  int SHARE_TRIP = 1;
}
