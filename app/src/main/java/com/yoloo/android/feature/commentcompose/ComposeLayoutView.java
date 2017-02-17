package com.yoloo.android.feature.commentcompose;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

public interface ComposeLayoutView extends MvpView {

  void onSuggestionsLoaded(List<AccountRealm> suggestions);

  void onNewComment(CommentRealm comment);

  void onError(Throwable throwable);
}
