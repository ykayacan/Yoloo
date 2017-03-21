package com.yoloo.android.feature.chat.dialoglist;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.framework.MvpView;

public interface DialogListView extends MvpView {

  void onMeLoaded(AccountRealm me);

  void onDialogAdded(Chat chat);

  void onDialogChanged(Chat chat);

  void onDialogRemoved(Chat chat);

  void onError(Throwable throwable);
}
