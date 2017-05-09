package com.yoloo.android.feature.writecommentbox;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface CommentInputView extends MvpView {

  void onMeLoaded(AccountRealm me);

  void onSuggestionsLoaded(List<AccountRealm> suggestions);

  void onNewComment(CommentRealm comment);

  void onError(Throwable throwable);
}
