package com.yoloo.backend.travelertype;

import com.google.api.server.spi.config.Transformer;

public class TravelerTypeTransformer implements Transformer<TravelerTypeEntity, TravelerType> {
  @Override
  public TravelerType transformTo(TravelerTypeEntity in) {
    return TravelerType.builder()
        .id(in.getWebsafeId())
        .name(in.getName())
        .imageUrl(in.getImageUrl().getValue())
        .build();
  }

  @Override
  public TravelerTypeEntity transformFrom(TravelerType in) {
    return null;
  }
}
