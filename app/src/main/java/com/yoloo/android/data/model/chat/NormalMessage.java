package com.yoloo.android.data.model.chat;

import com.annimon.stream.Stream;
import com.yoloo.android.chatkit.commons.models.IMessage;
import com.yoloo.android.chatkit.commons.models.IUser;
import com.yoloo.android.data.model.firebase.Chat;

import java.util.Date;
import java.util.Map;

import timber.log.Timber;

public class NormalMessage implements IMessage {

  private final Chat chat;

  public static NormalMessage of(Chat chat) {
    return new NormalMessage(chat);
  }

  private NormalMessage(Chat chat) {
    this.chat = chat;
  }

  @Override public String getId() {
    return chat.getId();
  }

  @Override public String getText() {
    return chat.getLastMessage();
  }

  @Override public IUser getUser() {
    Timber.d("getUser: %s", chat.getMembers().values());
    return Stream.of(chat.getMembers())
        .map(Map.Entry::getValue)
        .filter(value -> value.getId().equals(chat.getLastSenderId()))
        .map(NormalUser::of)
        .single();
  }

  @Override public Date getCreatedAt() {
    return new Date(chat.getTimestampUpdated());
  }
}
