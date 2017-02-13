package com.yoloo.android.feature.chat.compose;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

public interface ComposeView extends MvpDataView<Response<List<AccountRealm>>> {
}
