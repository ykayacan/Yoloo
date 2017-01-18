package com.yoloo.backend.tag;

import com.yoloo.backend.util.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class TagControllerFactory implements ControllerFactory<TagController> {

  @Override
  public TagController create() {
    return TagController.create(
        TagService.create(),
        TagShardService.create()
    );
  }
}
