package com.yoloo.android.feature.notification;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class NotificationPresenter extends MvpPresenter<NotificationView> {

  private final NotificationRepository notificationRepository;

  public NotificationPresenter(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Override public void onAttachView(NotificationView view) {
    super.onAttachView(view);
    loadNotifications(false, null, 20);
  }

  public void loadNotifications(boolean pullToRefresh, String cursor, int limit) {
    if (pullToRefresh) {
      getView().onLoading(pullToRefresh);
    }

    Disposable d = notificationRepository.list(cursor, null, limit)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showNotifications, this::showError);

    getDisposable().add(d);
  }

  private void showNotifications(Response<List<NotificationRealm>> response) {
    getView().onLoaded(response);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}
