package com.yoloo.backend.checkin.dto;

import java.util.Date;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CheckInDTO {
  private String id;
  private String ownerId;
  private float latitude;
  private float longitude;
  private Date created;
}
