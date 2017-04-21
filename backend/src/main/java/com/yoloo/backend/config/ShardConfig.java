package com.yoloo.backend.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ShardConfig {

  public static final int POST_SHARD_COUNTER = 3;

  public static final int BLOG_SHARD_COUNTER = 2;

  public static final int TAG_SHARD_COUNTER = 5;

  public static final int ACCOUNT_SHARD_COUNTER = 4;

  public static final int COMMENT_SHARD_COUNTER = 2;

  public static final int GROUP_SHARD_COUNTER = 3;
}
