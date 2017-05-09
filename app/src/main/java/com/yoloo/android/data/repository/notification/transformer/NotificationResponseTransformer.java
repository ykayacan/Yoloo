package com.yoloo.android.data.repository.notification.transformer;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.NotificationRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponseNotification;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.util.Collections;
import java.util.List;

public class NotificationResponseTransformer implements
    ObservableTransformer<CollectionResponseNotification, Response<List<NotificationRealm>>> {

  public static NotificationResponseTransformer create() {
    return new NotificationResponseTransformer();
  }

  @Override
  public ObservableSource<Response<List<NotificationRealm>>> apply(
      Observable<CollectionResponseNotification> upstream) {
    return upstream.map(
        response -> Response.create(getNotifications(response), response.getNextPageToken()));
  }

  private List<NotificationRealm> getNotifications(CollectionResponseNotification response) {
    return response.getItems() == null
        ? Collections.emptyList()
        : Stream.of(response.getItems()).map(NotificationRealm::new).toList();
  }
}
