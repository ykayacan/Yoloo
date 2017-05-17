package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface PostDetailView extends MvpDataView<List<FeedItem<?>>> {

  void onPostLoaded(PostRealm post);

  void onMeLoaded(AccountRealm me);

  void onPostUpdated(PostRealm post);

  void onCommentUpdated(CommentRealm comment);

  void onMoreLoaded(List<FeedItem<?>> items);
}
