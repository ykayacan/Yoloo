package com.yoloo.android.data.repository.category;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.repository.category.datasource.CategoryDiskDataStore;
import com.yoloo.android.data.repository.category.datasource.CategoryRemoteDataStore;
import com.yoloo.android.data.sorter.CategorySorter;

import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

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

  public Observable<List<CategoryRealm>> listCategories(@Nonnull CategorySorter sorter, int limit) {
    Observable<List<CategoryRealm>> diskObservable = diskDataStore.list(sorter, limit)
        .subscribeOn(Schedulers.io());

    Observable<List<CategoryRealm>> remoteObservable = remoteDataStore.list(sorter, limit)
        .subscribeOn(Schedulers.io())
        .doOnNext(diskDataStore::addAll);

    return Observable.mergeDelayError(diskObservable, remoteObservable)
        .filter(categories -> !categories.isEmpty())
        .distinct();
  }

  public Observable<List<CategoryRealm>> listInterestedCategories(@Nonnull String userId) {
    return remoteDataStore.listInterestedCategories(userId).subscribeOn(Schedulers.io());
  }
}
