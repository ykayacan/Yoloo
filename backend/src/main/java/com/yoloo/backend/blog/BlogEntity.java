package com.yoloo.backend.blog;

import java.util.Collection;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BlogEntity {

  private Blog blog;
  private Collection<BlogCounterShard> shards;
}
