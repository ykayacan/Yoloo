package com.yoloo.backend.group.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TravelerGroup {
  private String id;
  private String name;
  private String imageUrl;
  private long subscriberCount;
  private long postCount;
  private double rank;
  private boolean subscribed;
}
