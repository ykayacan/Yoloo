package com.yoloo.backend.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardConfig {

  public static final int POST_SHARD_COUNTER = 5;

  public static final int BLOG_SHARD_COUNTER = 2;

  public static final int TAG_SHARD_COUNTER = 5;

  public static final int ACCOUNT_SHARD_COUNTER = 4;

  public static final int COMMENT_SHARD_COUNTER = 2;

  public static final int CATEGORY_SHARD_COUNTER = 3;
}
