package com.yoloo.android.feature.feed.common.listener;

import android.view.View;
import com.yoloo.android.data.db.MediaRealm;

public interface OnContentImageClickListener {
  void onContentImageClick(View v, MediaRealm media);
}
