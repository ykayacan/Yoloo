package com.yoloo.android.feature.writecommentbox;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface WriteCommentBoxView extends MvpView {

  void onSuggestionsLoaded(List<AccountRealm> suggestions);

  void onNewComment(CommentRealm comment);

  void onError(Throwable throwable);
}
