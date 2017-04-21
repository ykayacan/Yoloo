package com.yoloo.backend.travelertype;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TravelerType {
  private String id;
  private String name;
  private String imageUrl;
}
