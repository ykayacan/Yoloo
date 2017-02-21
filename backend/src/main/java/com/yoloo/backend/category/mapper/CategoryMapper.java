package com.yoloo.backend.category.mapper;

import com.google.inject.Singleton;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.category.dto.CategoryDTO;
import com.yoloo.backend.mapper.Mapper;

@Singleton
public class CategoryMapper implements Mapper<Category, CategoryDTO> {
  @Override public CategoryDTO map(Category category) {
    return CategoryDTO.builder()
        .id(category.getWebsafeId())
        .name(category.getName())
        .type(category.getType().name())
        .postCount(category.getPosts())
        .rank(category.getRank())
        .build();
  }
}
