package com.yoloo.backend.checkin;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.checkin.transformer.CheckInTransformer;

import org.joda.time.DateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@ApiTransformer(CheckInTransformer.class)
public class CheckIn {

  @Id
  private Long id;

  @Parent
  private Key<Account> parent;

  private GeoPt location;

  private DateTime created;

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getWebsafeOwnerId() {
    return parent.toWebSafeString();
  }

  public Key<CheckIn> getKey() {
    return Key.create(parent, getClass(), id);
  }
}
