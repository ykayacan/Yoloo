package com.yoloo.backend.country.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Name", "Capital", "GeoRectangle", "SeqID", "GeoPt", "TelPref", "CountryCodes", "CountryInfo"
})
public class Results {

  @JsonProperty("Name") private String name;
  @JsonProperty("Capital") private Capital capital;
  @JsonProperty("GeoRectangle") private GeoRectangle geoRectangle;
  @JsonProperty("SeqID") private int seqID;
  @JsonProperty("GeoPt") private List<Double> geoPt = null;
  @JsonProperty("TelPref") private String telPref;
  @JsonProperty("CountryCodes") private CountryCodes countryCodes;
  @JsonProperty("CountryInfo") private String countryInfo;
}
