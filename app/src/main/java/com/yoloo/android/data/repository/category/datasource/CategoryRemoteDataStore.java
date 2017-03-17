package com.yoloo.android.data.repository.category.datasource;

import com.annimon.stream.Stream;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.sorter.CategorySorter;
import io.reactivex.Observable;
import java.util.List;

import static com.yoloo.android.data.ApiManager.INSTANCE;

public class CategoryRemoteDataStore {

  private static CategoryRemoteDataStore instance;

  private CategoryRemoteDataStore() {
  }

  public static CategoryRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new CategoryRemoteDataStore();
    }
    return instance;
  }

  public Observable<List<CategoryRealm>> list(CategorySorter sorter, int limit) {
    return Observable.fromCallable(() ->
        INSTANCE.getApi()
            .categories()
            .list()
            .setSort(sorter.name())
            .setLimit(limit)
            .execute())
        .map(response -> Stream.of(response.getItems()).map(CategoryRealm::new).toList());
  }
}
