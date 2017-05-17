package com.yoloo.android.feature.models.comment;

import android.support.annotation.NonNull;
import com.yoloo.android.data.db.CommentRealm;

public interface CommentCallbacks {

  void onCommentLongClickListener(@NonNull CommentRealm comment);

  void onCommentProfileClickListener(@NonNull String userId);

  void onCommentMentionClickListener(@NonNull String username);

  void onCommentVoteClickListener(@NonNull CommentRealm comment, int direction);

  void onCommentAcceptRequestClickListener(@NonNull CommentRealm comment);
}
