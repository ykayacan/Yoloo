package com.yoloo.android.data.repository.tag;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.datasource.TagDiskDataStore;
import com.yoloo.android.data.repository.tag.datasource.TagRemoteDataStore;
import com.yoloo.android.data.sorter.TagSorter;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TagRepository {

  private static TagRepository instance;

  private final TagRemoteDataStore remoteDataStore;
  private final TagDiskDataStore diskDataStore;

  private TagRepository(TagRemoteDataStore remoteDataStore, TagDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static TagRepository getInstance(TagRemoteDataStore remoteDataStore,
      TagDiskDataStore diskDataStore) {
    if (instance == null) {
      instance = new TagRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  public Observable<List<TagRealm>> listTags(TagSorter sorter) {
    return diskDataStore.list(sorter).subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<TagRealm>>> listTags(String name, String cursor, int limit) {
    return remoteDataStore
        .searchTag(name, cursor, limit)
        .doOnNext(response -> Observable
            .fromIterable(response.getData())
            .map(tag -> tag.setRecent(true))
            .toList()
            .toObservable()
            .subscribe(diskDataStore::addAll))
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<TagRealm>>> listTags2(String name, String cursor, int limit) {
    return diskDataStore
        .list(TagSorter.DEFAULT)
        .map(tagRealms -> Response.create(tagRealms, cursor))
        .subscribeOn(Schedulers.io());
  }

  public Observable<List<TagRealm>> listRecentTags() {
    return diskDataStore.listRecent().subscribeOn(Schedulers.io());
  }

  public Observable<List<TagRealm>> listRecommendedTags() {
    return remoteDataStore.listRecommendedTags();
  }

  public Observable<Response<List<TagRealm>>> searchTag(@Nonnull String query,
      @Nullable String cursor, int limit) {
    return remoteDataStore.searchTag(query, cursor, limit);
  }
}
