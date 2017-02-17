package com.yoloo.android.feature.comment;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface CommentView extends MvpDataView<Response<List<CommentRealm>>> {

  void onAccountLoaded(AccountRealm account);

  void onAcceptedCommentLoaded(CommentRealm comment);

  void onNewAccept(String commentId);

  void onCommentDeleted();
}
