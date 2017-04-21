package com.yoloo.backend.di.module;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.inject.Singleton;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.media.MediaService;
import dagger.Module;
import dagger.Provides;

@Module
public class ServiceModule {

  @Provides
  @Singleton
  public AccountShardService provideAccountShardService() {
    return AccountShardService.create();
  }

  @Provides
  @Singleton
  public GameService provideGameShardService() {
    return GameService.create();
  }

  @Provides
  @Singleton
  public ImagesService provideImagesService() {
    return ImagesServiceFactory.getImagesService();
  }

  @Provides
  @Singleton
  public MediaService provideMediaService() {
    return MediaService.create();
  }
}
