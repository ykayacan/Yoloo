package com.yoloo.backend.comment;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class CommentEntity {

  private Comment comment;
  private List<CommentShard> shards;
}
