package com.yoloo.android.feature.notification;

import android.content.Context;
import com.airbnb.epoxy.EpoxyAdapter;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.Collections;
import java.util.List;

class NotificationAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;

  private final CropCircleTransformation cropCircleTransformation;

  NotificationAdapter(Context context, OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    cropCircleTransformation = new CropCircleTransformation(context);
  }

  void addAll(List<NotificationRealm> notifications) {
    Stream.of(notifications)
        .forEach(notification -> addModel(new NotificationModel_()
            .notification(notification)
            .cropCircleTransformation(cropCircleTransformation)
            .onProfileClickListener(onProfileClickListener)));
  }

  void add(NotificationRealm notification) {
    addAll(Collections.singletonList(notification));
  }

  public void clear() {
    removeAllModels();
  }
}
