package com.yoloo.backend.comment.dto;

import java.util.Date;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CommentDTO {
  private String id;
  private String ownerId;
  private String postId;
  private String username;
  private String avatarUrl;
  private String content;
  private boolean accepted;
  private int direction;
  private long voteCount;
  private Date created;
}
