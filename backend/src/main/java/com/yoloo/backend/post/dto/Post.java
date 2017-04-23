package com.yoloo.backend.post.dto;

import com.yoloo.backend.media.dto.Media;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Post {
  private String id;
  private String ownerId;
  private String username;
  private String avatarUrl;
  private String content;
  @Nullable private String acceptedCommentId;
  @Nullable private String title;
  @Nullable private List<Media> medias;
  private Set<String> tags;
  private String group;
  private int bounty;
  private double rank;
  private int direction;
  private long voteCount;
  private long commentCount;
  private int reportCount;
  private int postType;
  private Date created;
}
