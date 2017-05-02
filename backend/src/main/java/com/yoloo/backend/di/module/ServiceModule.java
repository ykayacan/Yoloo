package com.yoloo.backend.di.module;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.media.MediaService;
import dagger.Module;
import dagger.Provides;

@Module
public class ServiceModule {

  @Provides
  public AccountShardService provideAccountShardService() {
    return AccountShardService.create();
  }

  @Provides
  public GameService provideGameShardService() {
    return GameService.create();
  }

  @Provides
  public ImagesService provideImagesService() {
    return ImagesServiceFactory.getImagesService();
  }

  @Provides
  public MediaService provideMediaService() {
    return MediaService.create();
  }
}
