package com.yoloo.backend.account;

import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.game.GamificationService;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class AccountControllerProvider implements ControllerFactory<AccountController> {
  @Override public AccountController create() {
    return AccountController.create(AccountShardService.create(), GamificationService.create());
  }
}
