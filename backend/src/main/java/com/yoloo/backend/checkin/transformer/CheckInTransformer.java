package com.yoloo.backend.checkin.transformer;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.checkin.CheckIn;
import com.yoloo.backend.checkin.dto.CheckInDTO;

public class CheckInTransformer implements Transformer<CheckIn, CheckInDTO> {
  @Override public CheckInDTO transformTo(CheckIn in) {
    return CheckInDTO.builder()
        .id(in.getWebsafeId())
        .ownerId(in.getWebsafeOwnerId())
        .latitude(in.getLocation().getLatitude())
        .longitude(in.getLocation().getLongitude())
        .created(in.getCreated().toDate())
        .build();
  }

  @Override public CheckIn transformFrom(CheckInDTO in) {
    return null;
  }
}
