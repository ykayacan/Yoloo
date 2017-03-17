package com.yoloo.backend.notification;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.notification.transformer.NotificationTransformer;
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
@ApiTransformer(NotificationTransformer.class)
public class Notification {

  public static final String FIELD_CREATED = "created";

  @Id
  private Long id;

  @NonFinal
  private Key<Account> senderKey;

  @Parent
  @NonFinal
  private Key<Account> receiverKey;

  @NonFinal
  private String senderUsername;

  @NonFinal
  private Link senderAvatarUrl;

  @NonFinal
  private Action action;

  @Singular
  @NonFinal
  private Map<String, Object> payloads;

  @Index
  @NonFinal
  private DateTime created;

  public Key<Notification> getKey() {
    return Key.create(receiverKey, Notification.class, id);
  }

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getSenderId() {
    return senderKey.toWebSafeString();
  }
}
