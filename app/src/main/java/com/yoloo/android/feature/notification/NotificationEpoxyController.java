package com.yoloo.android.feature.notification;

import android.content.Context;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.NotificationRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class NotificationEpoxyController extends TypedEpoxyController<List<NotificationRealm>> {

  private final OnProfileClickListener onProfileClickListener;

  private final CropCircleTransformation cropCircleTransformation;

  NotificationEpoxyController(Context context, OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.cropCircleTransformation = new CropCircleTransformation(context);
  }

  @Override
  protected void buildModels(List<NotificationRealm> notifications) {
    Stream
        .of(notifications)
        .forEach(notification -> new NotificationModel_()
            .id(notification.getId())
            .notification(notification)
            .cropCircleTransformation(cropCircleTransformation)
            .onProfileClickListener(onProfileClickListener)
            .addTo(this));
  }
}
