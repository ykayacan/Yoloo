package com.yoloo.backend.comment.transformer;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.dto.CommentDTO;

public class CommentTransformer implements Transformer<Comment, CommentDTO> {
  @Override public CommentDTO transformTo(Comment in) {
    return CommentDTO.builder()
        .id(in.getWebsafeId())
        .ownerId(in.getWebsafeOwnerId())
        .username(in.getUsername())
        .avatarUrl(in.getAvatarUrl().getValue())
        .content(in.getContent())
        .accepted(in.isAccepted())
        .direction(in.getDir().getValue())
        .voteCount(in.getVotes())
        .created(in.getCreated().toDate())
        .build();
  }

  @Override public Comment transformFrom(CommentDTO in) {
    return null;
  }
}
