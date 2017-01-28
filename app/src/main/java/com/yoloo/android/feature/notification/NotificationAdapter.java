package com.yoloo.android.feature.notification;

import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import java.util.Collections;
import java.util.List;

public class NotificationAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;

  public NotificationAdapter(OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;
  }

  public void addAll(List<NotificationRealm> notifications) {
    for (NotificationRealm n : notifications) {
      addModel(new NotificationModel_()
          .notification(n)
          .onProfileClickListener(onProfileClickListener));
    }
  }

  public void add(NotificationRealm notification) {
    addAll(Collections.singletonList(notification));
  }

  public void clear() {
    final int size = models.size();
    models.clear();
    notifyItemRangeRemoved(0, size);
  }
}
