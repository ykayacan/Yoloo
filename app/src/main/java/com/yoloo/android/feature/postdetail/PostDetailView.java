package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.FeedItem;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface PostDetailView extends MvpDataView<List<FeedItem>> {

  void onPostLoaded(PostRealm post);

  void onMeLoaded(AccountRealm me);

  void onPostUpdated(PostRealm post);

  void onCommentAccepted(String commentId);
}
