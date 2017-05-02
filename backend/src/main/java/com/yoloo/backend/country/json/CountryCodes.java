package com.yoloo.backend.country.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tld", "iso3", "iso2", "fips", "isoN"
})
public class CountryCodes {

  @JsonProperty("tld") private String tld;
  @JsonProperty("iso3") private String iso3;
  @JsonProperty("iso2") private String iso2;
  @JsonProperty("fips") private String fips;
  @JsonProperty("isoN") private int isoN;
}
