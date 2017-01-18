package com.yoloo.backend.follow;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.util.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class FollowContollerFactory implements ControllerFactory<FollowController> {

  @Override
  public FollowController create() {
    return FollowController.create(
        FollowService.create(),
        AccountShardService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService())
    );
  }
}
