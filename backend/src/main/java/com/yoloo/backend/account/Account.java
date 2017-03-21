package com.yoloo.backend.account;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.condition.IfNotNull;
import com.googlecode.objectify.condition.IfNull;
import com.yoloo.backend.account.condition.IfNotAdmin;
import com.yoloo.backend.account.transformer.AccountTransformer;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.country.Country;
import com.yoloo.backend.util.Deref;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ix.Ix;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ApiTransformer(AccountTransformer.class)
public class Account {

  public static final String FIELD_EMAIL = "email";
  public static final String FIELD_USERNAME = "username";
  public static final String FIELD_FIREBASE_UUID = "firebaseUUID";

  @Id
  private long id;

  @Index(value = IfNotAdmin.class)
  @Wither
  @NonFinal
  private String username;

  @Wither
  @NonFinal
  @IgnoreSave(IfNull.class)
  private String realname;

  @Index
  @Wither
  @NonFinal
  private Email email;

  @Index
  @IgnoreSave(value = IfNull.class)
  @NonFinal
  private String firebaseUUID;

  @Wither
  @NonFinal
  private Link avatarUrl;

  @Wither
  @NonFinal
  private String bio;

  @Wither
  @NonFinal
  private Link websiteUrl;

  @Load(ShardGroup.class)
  @NonFinal
  private List<Ref<AccountShard>> shardRefs;

  @Index
  @NonFinal
  private DateTime created;

  @NonFinal
  private String locale;

  @Wither
  @NonFinal
  private Gender gender;

  @NonFinal
  private DateTime birthDate;

  @NonFinal
  @Index(IfNotNull.class)
  private Set<Country> visitedCountries;

  @NonFinal
  @Index
  private Set<Key<Category>> interestedCategoryKeys;

  // Extra fields

  @Wither
  @Ignore
  @NonFinal
  private boolean isFollowing;

  @Wither
  @Ignore
  @NonFinal
  private Counts counts;

  @Ignore
  @Wither
  @NonFinal
  private Detail detail;

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public Key<Account> getKey() {
    return Key.create(Account.class, id);
  }

  public List<AccountShard> getShards() {
    return Deref.deref(getShardRefs());
  }

  public Set<String> getInterestedCategoryIds() {
    return interestedCategoryKeys == null
        ? Collections.emptySet()
        : Ix.from(interestedCategoryKeys).map(Key::toWebSafeString).toSet();
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