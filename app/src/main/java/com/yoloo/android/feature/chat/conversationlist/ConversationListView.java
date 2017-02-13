package com.yoloo.android.feature.chat.conversationlist;

import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.framework.MvpView;

public interface ConversationListView extends MvpView {

  void onChatAdded(Chat chat);

  void onChatChanged(Chat chat);

  void onChatRemoved(Chat chat);

  void onError(Throwable throwable);
}
