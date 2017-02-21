package com.yoloo.backend.game.badge;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Badge {

  private String name;
  private String displayName;
  private String badgePhotoUrl;
}
