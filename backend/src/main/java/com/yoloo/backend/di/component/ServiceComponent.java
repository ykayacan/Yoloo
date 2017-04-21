package com.yoloo.backend.di.component;

import com.google.inject.Singleton;
import com.yoloo.backend.account.UserEndpoint;
import com.yoloo.backend.di.module.ServiceModule;
import dagger.Component;

@Singleton
@Component(modules = { ServiceModule.class })
public interface ServiceComponent {
  void inject(UserEndpoint endpoint);
}
