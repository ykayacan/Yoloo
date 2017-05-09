package com.yoloo.android.feature.comment;

import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface CommentView extends MvpDataView<List<CommentRealm>> {

  void onCommentAccepted(CommentRealm acceptedComment);

  void onCommentDeleted();
}
