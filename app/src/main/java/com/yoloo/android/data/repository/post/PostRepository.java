package com.yoloo.android.data.repository.post;

import com.annimon.stream.Optional;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.sorter.PostSorter;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import timber.log.Timber;

public class PostRepository {

  private static PostRepository instance;

  private final PostRemoteDataStore remoteDataStore;
  private final PostDiskDataStore diskDataStore;

  private PostRepository(PostRemoteDataStore remoteDataStore, PostDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static PostRepository getInstance(PostRemoteDataStore remoteDataStore,
      PostDiskDataStore diskDataStore) {
    if (instance == null) {
      instance = new PostRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  public Observable<Optional<PostRealm>> getPost(@Nonnull String postId) {
    return Observable.mergeDelayError(
        diskDataStore.get(postId).subscribeOn(Schedulers.io()).toObservable(),
        remoteDataStore.get(postId)
            .doOnSuccess(diskDataStore::add)
            .map(Optional::of)
            .toObservable()
            .subscribeOn(Schedulers.io()));
  }

  public Single<PostRealm> addPost(@Nonnull PostRealm post) {
    return remoteDataStore.add(post)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  public Completable deletePost(@Nonnull String postId) {
    return remoteDataStore.delete(postId)
        .andThen(diskDataStore.delete(postId))
        .subscribeOn(Schedulers.io());
  }

  public Single<PostRealm> getDraft() {
    return diskDataStore.get("draft")
        .subscribeOn(Schedulers.io())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toSingle();
  }

  public Completable addDraft(@Nonnull PostRealm draft) {
    return Completable.fromAction(() -> diskDataStore.add(draft)).subscribeOn(Schedulers.io());
  }

  public Completable deleteDraft() {
    return diskDataStore.delete("draft").subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByFeed(@Nullable String cursor, int limit) {
    Timber.d("listByFeed()");
    Observable<Response<List<PostRealm>>> remoteObservable =
        remoteDataStore.listByFeed(cursor, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()))
            .subscribeOn(Schedulers.io())
            .doOnNext(response -> Timber.d("Response: %s", response.getData()));

    Observable<Response<List<PostRealm>>> diskObservable =
        diskDataStore.listByFeed().subscribeOn(Schedulers.io());

    return diskObservable;
  }

  public Observable<Response<List<PostRealm>>> listByBounty(@Nullable String cursor, int limit) {
    //return remoteDataStore.listByBounty(cursor, limit).subscribeOn(Schedulers.io());
    return diskDataStore.listByBounty().subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByCategory(@Nonnull String categoryName,
      @Nonnull PostSorter sorter, @Nullable String cursor, int limit) {
    return remoteDataStore.listByCategory(categoryName, sorter, cursor, limit)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByTags(@Nonnull String tagNames,
      @Nonnull PostSorter sorter, @Nullable String cursor, int limit) {
    return remoteDataStore.listByTags(tagNames, sorter, cursor, limit).subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByUser(@Nonnull String userId, boolean commented,
      @Nullable String cursor, int limit) {
    return remoteDataStore.listByUser(userId, commented, cursor, limit)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByBookmarked(@Nullable String cursor,
      int limit) {
    return Observable.mergeDelayError(
        diskDataStore.listBookmarkedPosts().subscribeOn(Schedulers.io()),
        remoteDataStore.listByBookmarked(cursor, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()))
            .subscribeOn(Schedulers.io()));
  }

  public Completable votePost(@Nonnull String postId, int direction) {
    return remoteDataStore.vote(postId, direction)
        .andThen(diskDataStore.vote(postId, direction).subscribeOn(Schedulers.io()));
  }

  public Completable bookmarkPost(@Nonnull String postId) {
    return remoteDataStore.bookmark(postId)
        .andThen(diskDataStore.bookmark(postId).subscribeOn(Schedulers.io()));
  }

  public Completable unBookmarkPost(@Nonnull String postId) {

    return remoteDataStore.unbookmark(postId)
        .andThen(diskDataStore.unBookmark(postId))
        .subscribeOn(Schedulers.io());
  }
}
