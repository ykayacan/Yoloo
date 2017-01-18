package com.yoloo.backend.notification;

import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.notification.action.Action;
import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder
public class Notification {

  @Id
  private Long id;

  private Key<Account> senderKey;

  @Parent
  private Key<Account> receiverKey;

  private String senderUsername;

  private Link senderAvatarUrl;

  private Action action;

  @Singular
  private Map<String, ?> objects;

  private DateTime created;
}
