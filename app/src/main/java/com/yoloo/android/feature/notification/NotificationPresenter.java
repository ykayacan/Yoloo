package com.yoloo.android.feature.notification;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

class NotificationPresenter extends MvpPresenter<NotificationView> {

  private final NotificationRepository notificationRepository;

  private String cursor;

  NotificationPresenter(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Override public void onAttachView(NotificationView view) {
    super.onAttachView(view);
    loadNotifications(false, 20);
  }

  void loadNotifications(boolean pullToRefresh, int limit) {
    shouldResetCursor(pullToRefresh);

    Disposable d = notificationRepository.listNotifications(cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .subscribe(this::showNotifications, this::showError);

    getDisposable().add(d);
  }

  private void showNotifications(Response<List<NotificationRealm>> response) {
    if (response.getData().isEmpty()) {
      getView().onEmpty();
    } else {
      cursor = response.getCursor();
      getView().onLoaded(response.getData());
      getView().showContent();
    }
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private void shouldResetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }
}
