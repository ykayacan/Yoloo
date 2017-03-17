package com.yoloo.backend.category;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.common.base.Optional;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.util.TestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CategoryControllerTest extends TestBase {

  private static final String USER_EMAIL = "test@gmail.com";
  private static final String USER_AUTH_DOMAIN = "gmail.com";

  @Override
  public void setUpGAE() {
    super.setUpGAE();

    helper.setEnvIsLoggedIn(true)
        .setEnvIsAdmin(true)
        .setEnvAuthDomain(USER_AUTH_DOMAIN)
        .setEnvEmail(USER_EMAIL);
  }

  @Test
  public void testAddCategory_continent() throws Exception {
    CategoryController categoryController = CategoryControllerFactory.of().create();

    Category category = categoryController.insertCategory("Camping", null);

    assertEquals("Camping", category.getName());
    assertEquals(0, category.getPosts());
    assertEquals(0.0, category.getRank(), 0);
    assertEquals(ShardConfig.CATEGORY_SHARD_COUNTER, category.getShards().size());
  }

  @Test
  public void testAddCategory_theme() throws Exception {
    CategoryController categoryController = CategoryControllerFactory.of().create();

    Category category = categoryController.insertCategory("Budget Travel", null);

    assertEquals("Budget Travel", category.getName());
    assertEquals(0, category.getPosts());
    assertEquals(0.0, category.getRank(), 0);
    assertEquals(ShardConfig.CATEGORY_SHARD_COUNTER, category.getShards().size());
  }

  @Test
  public void testUpdateCategory_continent() throws Exception {
    CategoryController categoryController = CategoryControllerFactory.of().create();

    Category original = categoryController.insertCategory("Budget Travel", null);

    assertEquals("Budget Travel", original.getName());
    assertEquals(0, original.getPosts());
    assertEquals(0.0, original.getRank(), 0);
    assertEquals(ShardConfig.CATEGORY_SHARD_COUNTER, original.getShards().size());

    Category updated = categoryController.updateCategory(original.getWebsafeId(),
        Optional.of("Female Travel"));

    assertEquals("Female Travel", updated.getName());
    assertEquals(0, updated.getPosts());
    assertEquals(0.0, updated.getRank(), 0);
    assertEquals(ShardConfig.CATEGORY_SHARD_COUNTER, updated.getShards().size());
  }

  @Test
  public void testListCategories_default() throws Exception {
    CategoryController categoryController = CategoryControllerFactory.of().create();

    categoryController.insertCategory("Budget Travel", null);
    categoryController.insertCategory("America", null);
    categoryController.insertCategory("Female Travel", null);

    CollectionResponse<Category> response =
        categoryController.listCategories(Optional.absent(), Optional.absent(), Optional.absent());

    assertEquals(3, response.getItems().size());
  }
}
