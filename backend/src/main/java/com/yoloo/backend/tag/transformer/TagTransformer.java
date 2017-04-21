package com.yoloo.backend.tag.transformer;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.dto.TagDTO;

public class TagTransformer implements Transformer<Tag, TagDTO> {
  @Override public TagDTO transformTo(Tag in) {
    return TagDTO.builder()
        .id(in.getWebsafeId())
        .name(in.getName())
        .postCount(in.getPostCount())
        .build();
  }

  @Override public Tag transformFrom(TagDTO in) {
    return null;
  }
}
