package com.yoloo.backend.post;

import com.googlecode.objectify.Ref;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class PostBundle {

  private PostEntity postEntity;
  private Map<Ref<PostShard>, PostShard> shards;
}
