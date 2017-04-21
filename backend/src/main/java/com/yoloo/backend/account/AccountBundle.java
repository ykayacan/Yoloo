package com.yoloo.backend.account;

import com.googlecode.objectify.Ref;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountBundle {

  private Account account;
  private Map<Ref<AccountShard>, AccountShard> shards;
}