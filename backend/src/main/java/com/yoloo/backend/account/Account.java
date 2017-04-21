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
import com.yoloo.backend.account.transformer.AccountTransformer;
import com.yoloo.backend.country.Country;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.util.Deref;
import ix.Ix;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder(toBuilder = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ApiTransformer(AccountTransformer.class)
@FieldDefaults(makeFinal = false)
public class Account {

  public static final String FIELD_EMAIL = "email";
  public static final String FIELD_USERNAME = "username";
  public static final String FIELD_SUBSCRIBED_GROUP_KEYS = "subscribedGroupKeys";

  @Id private long id;

  @Wither @Index(value = IfNotAdmin.class) private String username;

  @Wither @IgnoreSave(IfNull.class) private String realname;

  @Index @Wither private Email email;

  @Wither private Link avatarUrl;

  @Wither private String bio;

  @Wither private Link websiteUrl;

  @Load(ShardGroup.class) private List<Ref<AccountShard>> shardRefs;

  @Index private DateTime created;

  private String locale;

  private String country;

  @Wither private Gender gender;

  private DateTime birthDate;

  @Index(IfNotNull.class) private Set<Country> visitedCountries;

  @Index @Singular @Wither private List<Key<TravelerGroupEntity>> subscribedGroupKeys;

  // Extra fields

  @Wither @Ignore private boolean isFollowing;

  @Wither @Ignore private Counts counts;

  @Ignore @Wither private Detail detail;

  @Ignore @Wither private Map<Ref<AccountShard>, AccountShard> shardMap;

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public Key<Account> getKey() {
    return Key.create(Account.class, id);
  }

  public List<AccountShard> getShards() {
    return Deref.deref(getShardRefs());
  }

  public Set<String> getSubscribedGroupIds() {
    return subscribedGroupKeys == null
        ? Collections.emptySet()
        : Ix.from(subscribedGroupKeys).map(Key::toWebSafeString).toSet();
  }

  public enum Gender {
    /**
     * Male gender.
     */
    MALE, /**
     * Female gender.
     */
    FEMALE, /**
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