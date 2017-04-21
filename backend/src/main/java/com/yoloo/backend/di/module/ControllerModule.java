package com.yoloo.backend.di.module;

import com.google.appengine.api.images.ImagesService;
import com.google.inject.Singleton;
import com.yoloo.backend.account.AccountController;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.group.TravelerGroupService;
import com.yoloo.backend.media.MediaService;
import dagger.Module;
import dagger.Provides;

@Module
public class ControllerModule {

  @Provides
  @Singleton
  public AccountController provideUserController(
      AccountShardService accountShardService,
      GameService gameService,
      ImagesService imagesService,
      MediaService mediaService) {
    return AccountController.create(accountShardService,
        gameService,
        imagesService,
        mediaService,
        new TravelerGroupService());
  }
}
