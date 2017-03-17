package com.yoloo.backend.category;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.category.sort_strategy.CategorySorter;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.endpointsvalidator.Guard;
import com.yoloo.backend.util.KeyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
@AllArgsConstructor(staticName = "create")
public class CategoryController extends Controller {

  /**
   * Maximum number of categories to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 7;

  private CategoryShardService categoryShardService;

  private ImagesService imagesService;

  /**
   * Insert category category.
   *
   * @param displayName the name
   * @param imageName the image name
   * @return the category
   * @throws ConflictException the conflict exception
   */
  public Category insertCategory(String displayName, String imageName) throws ConflictException {
    Guard.checkConflictRequest(ofy().load().key(Category.createKey(displayName)).now(),
        "Category exists.");

    final Key<Category> categoryKey = Category.createKey(displayName);

    Map<Ref<CategoryShard>, CategoryShard> shardMap =
        categoryShardService.createShardMapWithRef(categoryKey);

    final String imageServingUrl;

    if (Strings.isNullOrEmpty(imageName)) {
      imageServingUrl = Strings.nullToEmpty(imageName);
    } else {
      ServingUrlOptions options = ServingUrlOptions.Builder
          .withGoogleStorageFileName(
              MediaConfig.CATEGORY_BUCKET + "/" + imageName.toLowerCase() + "@2x.webp");

      imageServingUrl = imagesService.getServingUrl(options);
    }

    Category category = Category.builder()
        .id(categoryKey.getName())
        .name(displayName)
        .imageUrl(new Link(imageServingUrl))
        .rank(0.0D)
        .posts(0L)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .build();

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(category)
        .addAll(shardMap.values())
        .build();

    return ofy().transact(() -> {
      ofy().save().entities(saveList).now();

      return category;
    });
  }

  /**
   * Update category category.
   *
   * @param categoryId the category id
   * @param name the name
   * @return the category
   */
  public Category updateCategory(String categoryId, Optional<String> name) {
    return Single.just(ofy().load().key(Key.<Category>create(categoryId)).now())
        .map(category -> name.isPresent() ? category.withName(name.get()) : category)
        .doOnSuccess(category -> ofy().save().entity(category).now())
        .blockingGet();
  }

  /**
   * List collection response.
   *
   * @param categoryIds the category ids
   * @param sorter the sorter
   * @param limit the limit
   * @param cursor the cursor
   * @return the collection response
   */
  public CollectionResponse<Category> listCategories(
      Optional<String> categoryIds,
      Optional<CategorySorter> sorter,
      Optional<Integer> limit,
      Optional<String> cursor) {

    List<Category> categories = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);
    String websafeCursor = null;

    if (categoryIds.isPresent()) {
      List<Key<Category>> categoryKeys = KeyUtil.extractKeysFromIds(categoryIds.get(), ",");
      categories = new ArrayList<>(ofy().load().group(Category.ShardGroup.class)
          .keys(categoryKeys).values());
    } else {
      Query<Category> query = ofy().load().group(Category.ShardGroup.class).type(Category.class);

      query = CategorySorter.sort(query, sorter.or(CategorySorter.DEFAULT));

      query = cursor.isPresent()
          ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
          : query;

      query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

      final QueryResultIterator<Category> qi = query.iterator();

      while (qi.hasNext()) {
        categories.add(qi.next());
      }

      websafeCursor = qi.getCursor().toWebSafeString();
    }

    return CollectionResponse.<Category>builder()
        .setItems(categories)
        .setNextPageToken(websafeCursor)
        .build();
  }
}
