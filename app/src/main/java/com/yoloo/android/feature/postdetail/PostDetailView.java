package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.annotation.PostType;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface PostDetailView extends MvpDataView<Response<List<CommentRealm>>> {

  void onPostLoaded(PostRealm post);

  void onPostUpdated(PostRealm post);

  void onCommentsLoaded(Response<List<CommentRealm>> value, String currentUserId, boolean postOwner,
      boolean accepted, @PostType int postType);

  void onAcceptedCommentLoaded(CommentRealm comment, boolean postOwner, @PostType int postType);

  void onNewCommentLoaded(CommentRealm comment, boolean postOwner, @PostType int postType);

  void onNewAccept(String commentId);

  void onMentionSuggestionsLoaded(List<AccountRealm> suggestions);
}
