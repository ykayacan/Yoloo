package com.yoloo.android.feature.notification;

import android.content.Context;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.NotificationRealm;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

class NotificationEpoxyController extends Typed2EpoxyController<List<NotificationRealm>, Boolean> {

  private final OnProfileClickListener onProfileClickListener;
  private final CropCircleTransformation cropCircleTransformation;

  @AutoModel LoaderModel loaderModel;

  private List<NotificationRealm> notifications;

  NotificationEpoxyController(Context context, OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.cropCircleTransformation = new CropCircleTransformation(context);

    this.notifications = new ArrayList<>();
  }

  public void showLoader() {
    setData(notifications, true);
  }

  public void hideLoader() {
    setData(notifications, false);
  }

  public void setLoadMoreData(List<NotificationRealm> items) {
    this.notifications.addAll(items);
    setData(this.notifications, false);
  }

  @Override public void setData(List<NotificationRealm> notifications, Boolean loadingMore) {
    this.notifications = notifications;
    super.setData(notifications, loadingMore);
  }

  @Override
  protected void buildModels(List<NotificationRealm> notifications, Boolean loadingMore) {
    Stream
        .of(notifications)
        .forEach(notification -> new NotificationModel_()
            .id(notification.getId())
            .notification(notification)
            .cropCircleTransformation(cropCircleTransformation)
            .onProfileClickListener(onProfileClickListener)
            .addTo(this));

    loaderModel.addIf(loadingMore, this);
  }
}
