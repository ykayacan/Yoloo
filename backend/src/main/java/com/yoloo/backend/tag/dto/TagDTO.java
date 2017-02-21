package com.yoloo.backend.tag.dto;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Builder
public class TagDTO {

  private String id;
  @Wither private String name;
  private String type;
  private long posts;
  private long totalTagCount;
}
