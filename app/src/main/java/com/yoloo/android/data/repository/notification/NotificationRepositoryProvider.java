package com.yoloo.android.data.repository.notification;

public class NotificationRepositoryProvider {
  public static NotificationRepository getRepository() {
    return NotificationRepository.getInstance(NotificationRemoteDataSource.getInstance(),
        NotificationDiskDataSource.getInstance());
  }
}
