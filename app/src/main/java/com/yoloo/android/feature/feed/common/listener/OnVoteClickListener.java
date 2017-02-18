package com.yoloo.android.feature.feed.common.listener;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface OnVoteClickListener {

  void onVoteClick(String votableId, int direction, @Type int type);

  @IntDef({
      Type.POST,
      Type.COMMENT
  })
  @Retention(RetentionPolicy.SOURCE)
  @interface Type {
    int POST = 0;
    int COMMENT = 1;
  }
}
