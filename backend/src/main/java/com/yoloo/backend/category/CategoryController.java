package com.yoloo.backend.category;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.category.sort_strategy.CategorySorter;
import com.yoloo.backend.endpointsvalidator.Guard;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class CategoryController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(CategoryController.class.getName());

  /**
   * Maximum number of categories to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 7;

  private CategoryShardService categoryShardService;

  /**
   * Insert category category.
   *
   * @param name the name
   * @param type the type
   * @return the category
   * @throws ConflictException the conflict exception
   */
  public Category insertCategory(String name, Category.Type type) throws ConflictException {
    Guard.checkConflictRequest(ofy().load().key(Category.createKey(name)).now(),
        "Category exists.");

    final Key<Category> categoryKey = Category.createKey(name);

    Map<Ref<CategoryShard>, CategoryShard> shardMap = createCategoryShardMap(categoryKey);

    Category category = Category.builder()
        .id(categoryKey.getName())
        .name(name)
        .type(type)
        .rank(0.0d)
        .posts(0L)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .build();

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(category)
        .addAll(shardMap.values())
        .build();

    return ofy().transact(() -> {
      Map<Key<Object>, Object> saved = ofy().save().entities(saveList).now();

      //noinspection SuspiciousMethodCalls
      return (Category) saved.get(categoryKey);
    });
  }

  /**
   * Update category category.
   *
   * @param categoryId the category id
   * @param name the name
   * @param type the type
   * @return the category
   */
  public Category updateCategory(
      String categoryId,
      Optional<String> name,
      Optional<Category.Type> type) {

    return Single.just(ofy().load().key(Key.<Category>create(categoryId)).now())
        .map(category -> name.isPresent() ? category.withName(name.get()) : category)
        .map(category -> type.isPresent() ? category.withType(type.get()) : category)
        .doOnSuccess(category -> ofy().save().entity(category).now())
        .blockingGet();
  }

  /**
   * List collection response.
   *
   * @param sorter the sorter
   * @param limit the limit
   * @param cursor the cursor
   * @return the collection response
   */
  public CollectionResponse<Category> listCategories(
      Optional<CategorySorter> sorter,
      Optional<Integer> limit,
      Optional<String> cursor) {

    // If sorter parameter is null, default sort strategy is "DEFAULT".
    CategorySorter categorySorter = sorter.or(CategorySorter.DEFAULT);

    Query<Category> query = setupCategoryListQuery(limit, cursor, categorySorter);

    final QueryResultIterator<Category> qi = query.iterator();

    List<Category> categories = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      categories.add(qi.next());
    }

    /*categories = CategoryUtil.mergeCounts(categories)
        .toList(DEFAULT_LIST_LIMIT)
        .blockingGet();*/

    return CollectionResponse.<Category>builder()
        .setItems(categories)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  private Query<Category> setupCategoryListQuery(Optional<Integer> limit, Optional<String> cursor,
      CategorySorter categorySorter) {
    // Init query fetch request.
    Query<Category> query = ofy().load().type(Category.class);

    // Sort by category sorter then edit query.
    query = CategorySorter.sort(query, categorySorter);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));
    return query;
  }

  private Map<Ref<CategoryShard>, CategoryShard> createCategoryShardMap(Key<Category> categoryKey) {
    return Observable.range(1, CategoryShard.SHARD_COUNT)
        .map(shardNum -> categoryShardService.createShard(categoryKey, shardNum))
        .toMap(Ref::create)
        .blockingGet();
  }
}
