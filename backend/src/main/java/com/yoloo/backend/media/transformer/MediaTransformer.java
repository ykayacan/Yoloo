package com.yoloo.backend.media.transformer;

import com.google.api.server.spi.config.Transformer;
import com.google.common.collect.ImmutableList;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.media.Size;
import com.yoloo.backend.media.dto.Media;
import com.yoloo.backend.media.size.LargeSize;
import com.yoloo.backend.media.size.LowSize;
import com.yoloo.backend.media.size.MediumSize;
import com.yoloo.backend.media.size.MiniSize;
import com.yoloo.backend.media.size.ThumbSize;
import java.util.List;

public class MediaTransformer implements Transformer<MediaEntity, Media> {
  @Override
  public Media transformTo(MediaEntity in) {
    List<Size> sizes = ImmutableList.of(MiniSize.of(in.getUrl()), ThumbSize.of(in.getUrl()),
        LowSize.of(in.getUrl()), MediumSize.of(in.getUrl()), LargeSize.of(in.getUrl()));

    return Media
        .builder()
        .id(in.getWebsafeId())
        .ownerId(in.getWebsafeOwnerId())
        .mime(in.getMime())
        .sizes(sizes)
        .build();
  }

  @Override
  public MediaEntity transformFrom(Media in) {
    return null;
  }
}
