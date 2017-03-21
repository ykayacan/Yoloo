package com.yoloo.backend.relationship;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.base.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class RelationshipControllerFactory implements ControllerFactory<RelationshipController> {

  @Override
  public RelationshipController create() {
    return RelationshipController.create(
        AccountShardService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService())
    );
  }
}
