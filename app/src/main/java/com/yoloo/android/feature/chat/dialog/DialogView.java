package com.yoloo.android.feature.chat.dialog;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.firebase.ChatMessage;
import com.yoloo.android.framework.MvpView;

public interface DialogView extends MvpView {

  void onMeLoaded(AccountRealm me);

  void onMessageAdded(ChatMessage message);

  void onMessageChanged(ChatMessage message);

  void onMessageRemoved(ChatMessage message);

  void onError(Throwable throwable);
}
