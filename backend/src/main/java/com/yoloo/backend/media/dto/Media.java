package com.yoloo.backend.media.dto;

import com.yoloo.backend.media.Size;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Media {
  private String id;
  private String ownerId;
  private String mime;
  private List<Size> sizes;
}
