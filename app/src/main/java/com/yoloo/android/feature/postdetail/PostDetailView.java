package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface PostDetailView extends MvpDataView<List<CommentRealm>> {

  void onAccountLoaded(AccountRealm account);

  void onPostLoaded(PostRealm post);

  void onPostUpdated(PostRealm post);

  void onAcceptedCommentLoaded(CommentRealm comment);

  void onNewAccept(String commentId);
}
