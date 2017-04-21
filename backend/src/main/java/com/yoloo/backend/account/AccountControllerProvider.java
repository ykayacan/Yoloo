package com.yoloo.backend.account;

import com.google.appengine.api.images.ImagesServiceFactory;
import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.group.TravelerGroupService;
import com.yoloo.backend.media.MediaService;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class AccountControllerProvider implements ControllerFactory<AccountController> {

  @Override
  public AccountController create() {
    return AccountController.create(AccountShardService.create(), GameService.create(),
        ImagesServiceFactory.getImagesService(), MediaService.create(), new TravelerGroupService());
  }
}
