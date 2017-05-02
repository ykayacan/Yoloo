package com.yoloo.backend.country.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "DLST", "TD", "Flg", "Name", "GeoPt"
})
public class Capital {
  @JsonProperty("DLST") private Double dLST;
  @JsonProperty("TD") private Double tD;
  @JsonProperty("Flg") private Integer flg;
  @JsonProperty("Name") private String name;
  @JsonProperty("GeoPt") private List<Double> geoPt = null;
}
