package com.yoloo.backend.country;

import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Entity
@Cache
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
public class Country {
  public static final String FIELD_NAME = "name";

  // ISO 3166-1 country code.
  @Id private String id;
  @Index private String name;
  private Link flagUrl;
}
