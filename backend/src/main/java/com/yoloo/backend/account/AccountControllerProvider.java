package com.yoloo.backend.account;

import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.base.ControllerFactory;
import com.yoloo.backend.country.CountryService;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.media.MediaService;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class AccountControllerProvider implements ControllerFactory<AccountController> {

  @Override
  public AccountController create() {
    return AccountController.create(AccountShardService.create(), GameService.create(),
        MediaService.create(), new CountryService(URLFetchServiceFactory.getURLFetchService(),
            ImagesServiceFactory.getImagesService()));
  }
}
