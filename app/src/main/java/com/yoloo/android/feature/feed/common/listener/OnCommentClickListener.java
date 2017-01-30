package com.yoloo.android.feature.feed.common.listener;

import android.view.View;
import com.yoloo.android.feature.comment.PostType;

public interface OnCommentClickListener {
  void onCommentClick(View v, String postId, String postOwnerId, String acceptedCommentId,
      @PostType int postType);
}
