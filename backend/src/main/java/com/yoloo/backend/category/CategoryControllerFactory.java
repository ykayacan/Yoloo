package com.yoloo.backend.category;

import com.yoloo.backend.base.ControllerFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "of")
public class CategoryControllerFactory implements ControllerFactory<CategoryController> {

  @Override
  public CategoryController create() {
    return CategoryController.create(CategoryShardService.create());
  }
}
