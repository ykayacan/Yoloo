package com.yoloo.android.feature.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ChatNotificationBroadcastReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context context, Intent intent) {
    Intent startServiceIntent = new Intent(context, NewChatListenerService.class);
    context.startService(startServiceIntent);
  }
}
