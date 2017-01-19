package com.yoloo.android.data.repository.tag;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.datasource.TagDiskDataStore;
import com.yoloo.android.data.repository.tag.datasource.TagRemoteDataStore;
import com.yoloo.android.data.sorter.TagSorter;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class TagRepository {

  private static TagRepository INSTANCE;

  private final TagRemoteDataStore remoteDataStore;
  private final TagDiskDataStore diskDataStore;

  private TagRepository(TagRemoteDataStore remoteDataStore, TagDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static TagRepository getInstance(TagRemoteDataStore remoteDataStore,
      TagDiskDataStore diskDataStore) {
    if (INSTANCE == null) {
      INSTANCE = new TagRepository(remoteDataStore, diskDataStore);
    }
    return INSTANCE;
  }

  public Observable<List<TagRealm>> list(TagSorter sorter) {
    return Observable.mergeDelayError(
        diskDataStore.list(sorter),
        remoteDataStore.list(sorter)
            .subscribeOn(Schedulers.io())
            .doOnNext(diskDataStore::replace));
  }

  public Observable<Response<List<TagRealm>>> list(String name, String cursor, int limit) {
    return remoteDataStore.list(name, cursor, limit)
        .subscribeOn(Schedulers.io())
        .doOnNext(response -> Observable.fromIterable(response.getData())
            .map(tag -> tag.setRecent(true))
            .toList()
            .toObservable()
            .subscribe(diskDataStore::addAll));
  }

  public Observable<List<TagRealm>> listRecent() {
    return diskDataStore.listRecent();
  }
}