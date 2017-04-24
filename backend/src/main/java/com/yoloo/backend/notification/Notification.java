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
import com.yoloo.backend.game.IfGameAction;
import com.yoloo.backend.notification.transformer.NotificationTransformer;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
@ApiTransformer(NotificationTransformer.class)
public class Notification {

  public static final String FIELD_ACTION = "action";
  public static final String FIELD_CREATED = "created";

  @Id private Long id;

  private Key<Account> senderKey;

  @Parent private Key<Account> receiverKey;

  private String senderUsername;

  private Link senderAvatarUrl;

  @Index(IfGameAction.class) private Action action;

  @Singular private Map<String, Object> payloads;

  @Index private DateTime created;

  public Key<Notification> getKey() {
    return Key.create(receiverKey, Notification.class, id);
  }

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getSenderId() {
    if (senderKey == null) {
      return null;
    }
    return senderKey.toWebSafeString();
  }
}
