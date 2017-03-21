package com.yoloo.backend.country;

import com.google.appengine.api.datastore.Link;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Country {
  private String iso3CountryCode;
  private Link flagUrl;
}
