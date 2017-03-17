package com.yoloo.android.feature.search;

import android.view.View;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.data.model.AccountRealm;

public interface OnFollowClickListener {
  void onFollowClick(View v, EpoxyModel<?> model, AccountRealm account, int direction);
}
