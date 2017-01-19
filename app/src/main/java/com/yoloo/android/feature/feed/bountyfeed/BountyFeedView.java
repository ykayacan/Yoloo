package com.yoloo.android.feature.feed.bountyfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.base.framework.MvpDataView;
import java.util.List;

public interface BountyFeedView extends MvpDataView<Response<List<PostRealm>>> {

  void onPostUpdated(PostRealm post);
}
