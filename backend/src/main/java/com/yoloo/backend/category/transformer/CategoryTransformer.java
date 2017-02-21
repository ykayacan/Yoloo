package com.yoloo.backend.category.transformer;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.category.dto.CategoryDTO;

public class CategoryTransformer implements Transformer<Category, CategoryDTO> {
  @Override public CategoryDTO transformTo(Category in) {
    return CategoryDTO.builder()
        .id(in.getWebsafeId())
        .name(in.getName())
        .type(in.getType().name())
        .postCount(in.getPosts())
        .rank(in.getRank())
        .build();
  }

  @Override public Category transformFrom(CategoryDTO in) {
    return null;
  }
}
