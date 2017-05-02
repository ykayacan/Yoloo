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
    "StatusMsg", "Results", "StatusCode"
})
public final class GeognosResponse {
  @JsonProperty("StatusMsg") private String statusMsg;
  @JsonProperty("Results") private Results results;
  @JsonProperty("StatusCode") private Integer statusCode;
}
