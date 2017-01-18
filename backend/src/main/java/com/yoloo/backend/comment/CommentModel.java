package com.yoloo.backend.comment;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder class CommentModel {

  private Comment comment;
  private List<CommentCounterShard> shards;
}
