package com.yoloo.android.data.repository.category.datasource;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.sorter.CategorySorter;
import io.reactivex.Observable;
import java.util.List;

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
    return Observable.empty();
  }
}
