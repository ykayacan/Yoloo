package com.yoloo.backend.travelertype;

import com.google.appengine.api.images.ImagesServiceFactory;
import com.yoloo.backend.base.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class TravelerTypeControllerFactory implements ControllerFactory<TravelerTypeController> {
  @Override
  public TravelerTypeController create() {
    return TravelerTypeController.create(ImagesServiceFactory.getImagesService());
  }
}
