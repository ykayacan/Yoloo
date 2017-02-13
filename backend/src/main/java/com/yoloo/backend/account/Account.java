package com.yoloo.backend.account;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.util.Deref;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

  public static final String FIELD_EMAIL = "email";
  public static final String FIELD_USERNAME = "username";
  public static final String FIELD_FIREBASE_UUID = "firebaseUUID";

  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private long id;

  @Index
  @Wither
  @NonFinal
  private String username;

  @Wither
  private String realname;

  @Index
  @Wither
  @NonFinal
  private Email email;

  @Index
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String firebaseUUID;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String password;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Provider provider;

  @Wither
  private Link avatarUrl;

  @Load(ShardGroup.class)
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Ref<AccountShard>> shardRefs;

  @Index
  @NonFinal
  private DateTime created;

  private String locale;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Gender gender;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private DateTime birthDate;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Key<Category>> categoryKeys;

  // Extra fields

  @Wither
  @Ignore
  private boolean isFollowing;

  @Wither
  @Ignore
  private Counts counts;

  @Ignore
  @Wither
  private Detail detail;

  @ApiResourceProperty(name = "id")
  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<Account> getKey() {
    return Key.create(Account.class, id);
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public List<AccountShard> getShards() {
    return Deref.deref(getShardRefs());
  }

  @ApiResourceProperty(name = "created")
  public Date getDate() {
    return created.toDate();
  }

  public enum Provider {
    /**
     * Google provider.
     */
    GOOGLE(1),
    /**
     * Facebook provider.
     */
    FACEBOOK(2),
    /**
     * Yoloo provider.
     */
    YOLOO(3);

    private final int provider;

    Provider(int provider) {
      this.provider = provider;
    }
  }

  public enum Gender {
    /**
     * Male gender.
     */
    MALE,
    /**
     * Female gender.
     */
    FEMALE,
    /**
     * Unspecified gender.
     */
    UNSPECIFIED
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ShardGroup {
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Counts {
    private long followings;
    private long followers;
    private long questions;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Detail {
    private int level;
    private int points;
    private int bounties;
  }
}