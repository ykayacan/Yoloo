package com.yoloo.backend.media.transformer;

import com.google.api.server.spi.config.Transformer;
import com.google.common.collect.ImmutableList;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.dto.MediaDTO;
import com.yoloo.backend.media.size.LargeSize;
import com.yoloo.backend.media.size.LowSize;
import com.yoloo.backend.media.size.MediumSize;
import com.yoloo.backend.media.size.MiniSize;
import com.yoloo.backend.media.size.ThumbSize;

public class MediaTransformer implements Transformer<Media, MediaDTO> {
  @Override public MediaDTO transformTo(Media in) {
    return MediaDTO.builder()
        .id(in.getWebsafeId())
        .ownerId(in.getWebsafeOwnerId())
        .mime(in.getMime())
        .sizes(ImmutableList.of(
            ThumbSize.of(in.getUrl()),
            MiniSize.of(in.getUrl()),
            LowSize.of(in.getUrl()),
            MediumSize.of(in.getUrl()),
            LargeSize.of(in.getUrl())
        ))
        .build();
  }

  @Override public Media transformFrom(MediaDTO in) {
    return null;
  }
}
