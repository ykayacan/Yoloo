package com.yoloo.android.feature.comment;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.feature.base.framework.MvpDataView;
import java.util.List;

public interface CommentView extends MvpDataView<Response<List<CommentRealm>>> {

  void onCommentLoaded(CommentRealm comment);

  void onAcceptedCommentLoaded(CommentRealm comment);

  void onMentionSuggestionsLoaded(List<AccountRealm> suggestions);
}
