package com.yoloo.backend.comment;

import com.googlecode.objectify.Ref;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class CommentEntity {

  private Comment comment;
  private Map<Ref<CommentShard>, CommentShard> shards;
}
