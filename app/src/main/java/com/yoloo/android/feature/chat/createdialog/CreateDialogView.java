package com.yoloo.android.feature.chat.createdialog;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.framework.MvpDataView;

import java.util.List;

interface CreateDialogView extends MvpDataView<List<AccountRealm>> {

  void onMeLoaded(AccountRealm me);

  void onUsersLoaded(List<AccountRealm> accounts);
}
