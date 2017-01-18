package com.yoloo.android.feature.feed.globalfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.base.framework.MvpDataView;
import java.util.List;

interface GlobalFeedView extends MvpDataView<Response<List<PostRealm>>> {
}
