package com.yoloo.backend.relationship;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.notification.NotificationService;

public class RelationshipControllerFactory implements ControllerFactory<RelationshipController> {

  private RelationshipControllerFactory() {
  }

  public static RelationshipControllerFactory of() {
    return new RelationshipControllerFactory();
  }

  @Override public RelationshipController create() {
    return RelationshipController.create(AccountShardService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService()));
  }
}
