package com.yoloo.backend.account;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountModel {

  private Account account;
  private List<AccountCounterShard> shards;
}