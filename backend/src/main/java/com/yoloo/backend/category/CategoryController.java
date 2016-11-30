package com.yoloo.backend.category;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.category.sort_strategy.CategorySorter;

import java.util.Map;
import java.util.logging.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public class CategoryController {

    private static final Logger logger =
            Logger.getLogger(CategoryController.class.getName());

    /**
     * Maximum number of categories to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 5;

    @NonNull
    private CategoryService categoryService;

    @NonNull
    private CategoryShardService categoryShardService;

    public Category add(String name, User user) {
        Category category = categoryService.create(name);

        ImmutableSet<CategoryCounterShard> shards =
                categoryShardService.createShards(category.getKey());

        ImmutableSet<Object> saveList = ImmutableSet.builder()
                .add(category)
                .addAll(shards)
                .build();

        ofy().save().entities(saveList);

        return category;
    }

    /**
     * List collection response.
     *
     * @param sorter the sorter
     * @param limit  the limit
     * @param cursor the cursor
     * @param user   the user
     * @return the collection response
     */
    public CollectionResponse<Category> list(Optional<CategorySorter> sorter,
                                             Optional<Integer> limit,
                                             Optional<String> cursor,
                                             User user) {
        // If sorter parameter is null, default sort strategy is "TRENDING".
        CategorySorter categorySorter = sorter.or(CategorySorter.DEFAULT);

        // Init query fetch request.
        Query<Category> query = ofy().load().type(Category.class);

        // Sort by category sorter then edit query.
        query = CategorySorter.sort(query, categorySorter);

        // Fetch items from beginning from cursor.
        query = cursor.isPresent()
                ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
                : query;

        // Limit items.
        query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

        final QueryResultIterator<Category> qi = query.iterator();

        Map<Key<Category>, Category> map = Maps.newLinkedHashMap();

        while (qi.hasNext()) {
            // Add fetched objects to map. Because cursor iteration needs to be iterated.
            Category item = qi.next();
            map.put(item.getKey(), item);
        }

        if (!map.isEmpty()) {
            map = CategoryUtil.aggregateCounts(map, categoryShardService);
        }

        return CollectionResponse.<Category>builder()
                .setItems(map.values())
                .setNextPageToken(qi.getCursor().toWebSafeString())
                .build();
    }
}
