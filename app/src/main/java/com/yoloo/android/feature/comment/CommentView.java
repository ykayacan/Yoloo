package com.yoloo.android.feature.comment;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

public interface CommentView extends MvpDataView<Response<List<CommentRealm>>> {

  void onCommentsLoaded(Response<List<CommentRealm>> value, String currentUserId, boolean postOwner,
      boolean accepted);

  void onAcceptedCommentLoaded(CommentRealm comment, boolean postOwner);

  void onNewCommentLoaded(CommentRealm comment, boolean postOwner);

  void onNewAccept(String commentId);

  void onMentionSuggestionsLoaded(List<AccountRealm> suggestions);

  void onCommentDeleted();
}
