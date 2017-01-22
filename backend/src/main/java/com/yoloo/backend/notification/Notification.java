package com.yoloo.backend.notification;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.notification.action.Action;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Notification {

  public static final String FIELD_CREATED = "created";

  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Long id;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Account> senderKey;

  private String senderId;

  @Parent
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Account> receiverKey;

  private String senderUsername;

  private Link senderAvatarUrl;

  private Action action;

  @Singular
  private Map<String, ?> objects;

  @Index
  @NonFinal
  private DateTime created;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<Notification> getKey() {
    return Key.create(receiverKey, Notification.class, id);
  }

  @ApiResourceProperty(name = "id")
  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }
}
