package com.yoloo.android.feature.blog;

import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface BlogView extends MvpDataView<List<CommentRealm>> {

  void onPostUpdated(PostRealm post);

  void onCommentUpdated(CommentRealm comment);
}
