package com.yoloo.android.data.repository.category;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.data.sorter.CategorySorter;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.Collections;
import java.util.List;

public class CategoryRepository {

  private static CategoryRepository instance;

  private final CategoryRemoteDataStore remoteDataStore;
  private final CategoryDiskDataStore diskDataStore;

  private CategoryRepository(CategoryRemoteDataStore remoteDataStore,
      CategoryDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static CategoryRepository getInstance(CategoryRemoteDataStore remoteDataStore,
      CategoryDiskDataStore diskDataStore) {
    if (instance == null) {
      instance = new CategoryRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  public void addCategory(CategoryRealm category) {
    addCategory(Collections.singletonList(category));
  }

  public void addCategory(List<CategoryRealm> categories) {
    diskDataStore.addAll(categories);
  }

  public Observable<List<CategoryRealm>> listCategories(int limit, CategorySorter sorter) {
    return Observable.mergeDelayError(
        diskDataStore.list(sorter).subscribeOn(Schedulers.io()),
        remoteDataStore.list(sorter, limit)
            .doOnNext(diskDataStore::addAll)
            .subscribeOn(Schedulers.io()));
  }
}