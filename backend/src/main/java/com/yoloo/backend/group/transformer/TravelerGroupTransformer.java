package com.yoloo.backend.group.transformer;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.group.dto.TravelerGroup;

public class TravelerGroupTransformer implements Transformer<TravelerGroupEntity, TravelerGroup> {

  @Override
  public TravelerGroup transformTo(TravelerGroupEntity in) {
    return TravelerGroup
        .builder()
        .id(in.getWebsafeId())
        .name(in.getName())
        .imageUrl(in.getImageUrl().getValue())
        .subscriberCount(in.getSubscriberCount())
        .postCount(in.getPostCount())
        .rank(in.getRank())
        .subscribed(in.isSubscribed())
        .build();
  }

  @Override
  public TravelerGroupEntity transformFrom(TravelerGroup in) {
    return null;
  }
}
