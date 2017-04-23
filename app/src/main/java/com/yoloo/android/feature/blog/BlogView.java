package com.yoloo.android.feature.blog;

import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface BlogView extends MvpDataView<List<CommentRealm>> {
  void onCommentAccepted(CommentRealm comment);
}
