package com.yoloo.android.feature.search;

import android.view.View;
import com.yoloo.android.data.db.AccountRealm;

public interface OnFollowClickListener {
  void onFollowClick(View v, AccountRealm account, int direction);
}
