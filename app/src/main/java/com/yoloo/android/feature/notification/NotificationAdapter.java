package com.yoloo.android.feature.notification;

import com.airbnb.epoxy.EpoxyAdapter;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import java.util.Collections;
import java.util.List;

class NotificationAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;

  NotificationAdapter(OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;
  }

  void addAll(List<NotificationRealm> notifications) {
    Stream.of(notifications)
        .forEach(notification -> addModel(new NotificationModel_()
            .notification(notification)
            .onProfileClickListener(onProfileClickListener)));
  }

  void add(NotificationRealm notification) {
    addAll(Collections.singletonList(notification));
  }

  public void clear() {
    removeAllModels();
  }
}
