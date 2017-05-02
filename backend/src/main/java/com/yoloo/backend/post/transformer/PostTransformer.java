package com.yoloo.backend.post.transformer;

import com.google.api.server.spi.config.Transformer;
import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.dto.Media;
import com.yoloo.backend.media.size.LargeSize;
import com.yoloo.backend.media.size.LowSize;
import com.yoloo.backend.media.size.MediumSize;
import com.yoloo.backend.media.size.ThumbSize;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.post.dto.Post;
import ix.Ix;
import java.util.Collections;

public class PostTransformer implements Transformer<PostEntity, Post> {
  @Override
  public Post transformTo(PostEntity in) {
    return Post
        .builder()
        .id(in.getWebsafeId())
        .ownerId(in.getWebsafeOwnerId())
        .username(in.getUsername())
        .avatarUrl(in.getAvatarUrl().getValue())
        .content(in.getContent())
        .acceptedCommentId(in.getAcceptedCommentId())
        .title(in.getTitle())
        .medias(in.getMedias() == null
            ? Collections.emptyList()
            : Ix.from(in.getMedias()).map(this::getMedia).toList())
        .tags(in.getTags())
        .group(TravelerGroupEntity.extractNameFromKey(in.getTravelerGroup()))
        .bounty(in.getBounty())
        .rank(in.getRank())
        .direction(in.getDir().getValue())
        .voteCount(in.getVoteCount())
        .commentCount(in.getCommentCount())
        .reportCount(in.getReportCount())
        .bookmarked(in.isBookmarked())
        .postType(in.getPostType())
        .created(in.getCreated().toDate())
        .build();
  }

  @Override
  public PostEntity transformFrom(Post in) {
    return null;
  }

  private Media getMedia(PostEntity.PostMedia in) {
    return Media
        .builder()
        .id(in.getMediaId())
        .mime(MediaType.WEBP.subtype())
        .sizes(ImmutableList.of(ThumbSize.of(in.getUrl()), LowSize.of(in.getUrl()),
            MediumSize.of(in.getUrl()), LargeSize.of(in.getUrl())))
        .build();
  }
}
