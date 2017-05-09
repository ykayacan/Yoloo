package com.yoloo.android.feature.feed.common.listener;

import com.yoloo.android.data.db.CommentRealm;

public interface OnCommentVoteClickListener {

  void onVoteClick(CommentRealm comment, int direction);
}
