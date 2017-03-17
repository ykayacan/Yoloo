package com.yoloo.backend.category.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryDTO {

  private String id;
  private String name;
  private String imageUrl;
  private long postCount;
  private double rank;
}
