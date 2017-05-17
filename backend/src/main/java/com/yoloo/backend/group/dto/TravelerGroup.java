package com.yoloo.backend.group.dto;

import com.yoloo.backend.group.TravelerGroupEntity;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TravelerGroup {
  private String id;
  private String name;
  private String imageWithIconUrl;
  private String imageWithoutIconUrl;
  private long subscriberCount;
  private List<TravelerGroupEntity.GroupSubscriber> topSubscribers;
  private long postCount;
  private double rank;
  private boolean subscribed;
}
