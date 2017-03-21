package com.yoloo.android.data.model.chat.chatbot;

import android.os.Parcel;

import com.yoloo.android.chatkit.commons.models.IMessage;
import com.yoloo.android.chatkit.commons.models.IUser;
import com.yoloo.android.data.model.chat.NormalDialog;
import com.yoloo.android.data.model.chat.NormalUser;

import java.util.Collections;
import java.util.Date;

public class ChatBotDialog extends NormalDialog {

  public ChatBotDialog() {
    super("dialog.chatbot", "Yoloo ChatBot",
        "https://storage.googleapis.com/yoloo-151719.appspot.com/system-default/empty_user_avatar"
            + ".webp",
        Collections.singletonList(new ChatBotUser()), new ChatBotMessage(), 0);
  }

  protected ChatBotDialog(Parcel in) {
    super(in);
  }

  public static class ChatBotUser extends NormalUser {

    ChatBotUser() {
      super("user.chatbot", "Yoloo ChatBot",
          "https://storage.googleapis.com/yoloo-151719.appspot.com/"
              + "system-default/empty_user_avatar.webp");
    }
  }

  public static class ChatBotMessage implements IMessage {

    @Override public String getId() {
      return "message.chatbot";
    }

    @Override public String getText() {
      return null;
    }

    @Override public IUser getUser() {
      return new ChatBotUser();
    }

    @Override public Date getCreatedAt() {
      return new Date();
    }
  }
}
