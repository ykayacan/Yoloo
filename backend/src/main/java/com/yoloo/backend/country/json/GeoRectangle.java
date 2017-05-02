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
    "West", "East", "North", "South"
})
public class GeoRectangle {

  @JsonProperty("West") private double west;
  @JsonProperty("East") private double east;
  @JsonProperty("North") private double north;
  @JsonProperty("South") private double south;
}
