package com.yoloo.backend.group;

import com.google.appengine.api.images.ImagesServiceFactory;
import com.yoloo.backend.base.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class TravelerGroupControllerFactory implements ControllerFactory<TravelerGroupController> {

  @Override
  public TravelerGroupController create() {
    return TravelerGroupController.create(TravelerGroupShardService.create(),
        ImagesServiceFactory.getImagesService());
  }
}
