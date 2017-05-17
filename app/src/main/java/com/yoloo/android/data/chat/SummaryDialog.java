package com.yoloo.android.data.chat;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IUser;
import com.yoloo.android.data.chat.firebase.Chat;
import com.yoloo.android.data.db.AccountRealm;
import java.util.List;

public class SummaryDialog implements IDialog<LastMessage> {

  private final Chat chat;
  private final List<SummaryDialogUser> users;
  private LastMessage lastMessage;

  public SummaryDialog(Chat chat, List<SummaryDialogUser> users, LastMessage lastMessage) {
    this.chat = chat;
    this.users = users;
    this.lastMessage = lastMessage;
  }

  @Override public String getId() {
    return chat.getChatId();
  }

  @Override public String getDialogPhoto() {
    return chat.getChatPhoto();
  }

  @Override public String getDialogName() {
    return chat.getChatName();
  }

  @Override public List<? extends IUser> getUsers() {
    return users;
  }

  @Override public LastMessage getLastMessage() {
    return lastMessage;
  }

  @Override public void setLastMessage(LastMessage message) {
    this.lastMessage = message;
  }

  @Override public int getUnreadCount() {
    return chat.getUnreadCount();
  }

  @Override public String toString() {
    return "SummaryDialog{" +
        "chat=" + chat +
        ", lastMessage=" + lastMessage +
        '}';
  }

  public static final class SummaryDialogUser implements IUser {

    private final String id;
    private final String username;
    private final String avatar;

    public SummaryDialogUser(AccountRealm account) {
      this(account.getId(), account.getUsername(), account.getAvatarUrl());
    }

    public SummaryDialogUser(String id, String username, String avatar) {
      this.id = id;
      this.username = username;
      this.avatar = avatar;
    }

    @Override public String getId() {
      return id;
    }

    @Override public String getName() {
      return username;
    }

    @Override public String getAvatar() {
      return avatar;
    }

    @Override public String toString() {
      return "SummaryDialogUser{" +
          "id='" + id + '\'' +
          ", username='" + username + '\'' +
          ", avatar='" + avatar + '\'' +
          '}';
    }
  }
}
