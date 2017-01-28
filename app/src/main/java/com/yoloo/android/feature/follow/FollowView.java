package com.yoloo.android.feature.follow;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.base.framework.MvpDataView;
import java.util.List;

public interface FollowView extends MvpDataView<Response<List<AccountRealm>>> {
}
