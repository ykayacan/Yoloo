package com.yoloo.android.feature.chat.conversationlist;

import com.airbnb.epoxy.EpoxyModel;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;

class ConversationListAdapter extends SelectableAdapter {

  private final OnItemClickListener<Chat> onItemClickListener;
  private final OnItemLongClickListener<Chat> onItemLongClickListener;

  ConversationListAdapter(OnItemClickListener<Chat> onItemClickListener,
      OnItemLongClickListener<Chat> onItemLongClickListener) {
    this.onItemClickListener = onItemClickListener;
    this.onItemLongClickListener = onItemLongClickListener;
  }

  void addChat(Chat chat) {
    addModel(new ConversationListModel_()
        .chat(chat)
        .adapter(this)
        .onItemLongClickListener(onItemLongClickListener)
        .itemClickListener(onItemClickListener));
  }

  void changeChat(Chat chat) {
    notifyModelChanged(findModel(chat), chat);
  }

  void removeChat(Chat chat) {
    removeModel(findModel(chat));
  }

  void removeChat(EpoxyModel<?> model) {
    removeModel(model);
  }

  private ConversationListModel findModel(Chat chat) {
    return Stream.of(models)
        .select(ConversationListModel.class)
        .filter(value -> value.getChat().getId().equals(chat.getId()))
        .single();
  }
}
