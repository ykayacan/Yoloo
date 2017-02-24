package com.yoloo.backend.post.transformer;

import com.google.api.server.spi.config.Transformer;
import com.google.common.collect.ImmutableList;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.dto.MediaDTO;
import com.yoloo.backend.media.size.LargeSize;
import com.yoloo.backend.media.size.LowSize;
import com.yoloo.backend.media.size.MediumSize;
import com.yoloo.backend.media.size.ThumbSize;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.post.dto.PostDTO;

public class PostTransformer implements Transformer<Post, PostDTO> {
  @Override public PostDTO transformTo(Post in) {
    return PostDTO.builder()
        .id(in.getWebsafeId())
        .ownerId(in.getWebsafeOwnerId())
        .username(in.getUsername())
        .avatarUrl(in.getAvatarUrl().getValue())
        .content(in.getContent())
        .acceptedCommentId(in.getAcceptedCommentId())
        .title(in.getTitle())
        .media(getMediaDTO(in.getMedia()))
        .tags(in.getTags())
        .categories(in.getCategories())
        .bounty(in.getBounty())
        .rank(in.getRank())
        .direction(in.getDir().getValue())
        .voteCount(in.getVoteCount())
        .commentCount(in.getCommentCount())
        .reportCount(in.getReportCount())
        .type(in.getPostType().name().toLowerCase())
        .created(in.getCreated().toDate())
        .build();
  }

  @Override public Post transformFrom(PostDTO in) {
    return null;
  }

  private MediaDTO getMediaDTO(Media in) {
    if (in == null) {
      return null;
    }

    return MediaDTO.builder()
        .id(in.getId())
        .mime(in.getMime())
        .sizes(ImmutableList.of(
            ThumbSize.of(in.getUrl()),
            LowSize.of(in.getUrl()),
            MediumSize.of(in.getUrl()),
            LargeSize.of(in.getUrl())
        ))
        .build();
  }
}
