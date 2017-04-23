package com.yoloo.android.feature.comment;

import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface CommentView extends MvpDataView<List<CommentRealm>> {

  void onNewAccept(String commentId);

  void onCommentDeleted();
}
