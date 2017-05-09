package com.yoloo.android.feature.feed.common.listener;

import com.yoloo.android.data.db.PostRealm;

public interface OnVoteClickListener {

  void onPostVoteClick(PostRealm post, int direction);
}
