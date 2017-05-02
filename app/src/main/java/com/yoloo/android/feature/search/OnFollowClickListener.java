package com.yoloo.android.feature.search;

import android.view.View;
import com.yoloo.android.data.model.AccountRealm;

public interface OnFollowClickListener {
  void onFollowClick(View v, AccountRealm account, int direction);
}
