package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.base.framework.MvpDataView;
import java.util.List;

interface PostDetailView extends MvpDataView<Response<List<CommentRealm>>> {

  void onPostLoaded(PostRealm post);

  void onCommentLoaded(CommentRealm comment);

  void onAcceptedCommentLoaded(CommentRealm comment);

  void onPostUpdated(PostRealm post);

  void onMentionSuggestionsLoaded(List<AccountRealm> suggestions);
}
