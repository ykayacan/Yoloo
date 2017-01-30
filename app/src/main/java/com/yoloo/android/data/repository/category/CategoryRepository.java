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

  private static CategoryRepository INSTANCE;

  private final CategoryRemoteDataStore remoteDataStore;
  private final CategoryDiskDataStore diskDataStore;

  private CategoryRepository(CategoryRemoteDataStore remoteDataStore,
      CategoryDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static CategoryRepository getInstance(CategoryRemoteDataStore remoteDataStore,
      CategoryDiskDataStore diskDataStore) {
    if (INSTANCE == null) {
      INSTANCE = new CategoryRepository(remoteDataStore, diskDataStore);
    }
    return INSTANCE;
  }

  public void add(CategoryRealm category) {
    add(Collections.singletonList(category));
  }

  public void add(List<CategoryRealm> categories) {
    diskDataStore.addAll(categories);
  }

  public Observable<List<CategoryRealm>> list(int limit, CategorySorter sorter) {
    return Observable.mergeDelayError(
        diskDataStore.list(sorter).subscribeOn(Schedulers.io()),
        remoteDataStore.list(sorter, limit)
            .doOnNext(diskDataStore::addAll)
            .subscribeOn(Schedulers.io()));
  }
}