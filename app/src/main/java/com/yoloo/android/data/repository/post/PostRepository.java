package com.yoloo.android.data.repository.post;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.util.Preconditions;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class PostRepository {

  private static PostRepository INSTANCE;

  private final PostRemoteDataStore remoteDataStore;
  private final PostDiskDataStore diskDataStore;

  private PostRepository(PostRemoteDataStore remoteDataStore, PostDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static PostRepository getInstance(PostRemoteDataStore remoteDataStore,
      PostDiskDataStore diskDataStore) {
    if (INSTANCE == null) {
      INSTANCE = new PostRepository(remoteDataStore, diskDataStore);
    }
    return INSTANCE;
  }

  public Observable<PostRealm> get(String postId) {
    Preconditions.checkNotNull(postId, "postId can not be null.");

    return Observable.mergeDelayError(
        diskDataStore.get(postId).subscribeOn(Schedulers.io()),
        remoteDataStore.get(postId)
            .doOnNext(postRealm -> diskDataStore.add(postRealm).subscribe())
            .subscribeOn(Schedulers.io()));
  }

  public Observable<PostRealm> add(PostRealm post) {
    Preconditions.checkNotNull(post, "post can not be null.");

    return remoteDataStore.add(post)
        .doOnNext(postRealm -> diskDataStore.add(postRealm).subscribe())
        .subscribeOn(Schedulers.io());
  }

  public Completable delete(String postId) {
    Preconditions.checkNotNull(postId, "postId can not be null.");

    return remoteDataStore.delete(postId)
        .andThen(diskDataStore.delete(postId))
        .subscribeOn(Schedulers.io());
  }

  public Observable<PostRealm> getDraft() {
    return diskDataStore.get("draft").subscribeOn(Schedulers.io());
  }

  public Completable addDraft(PostRealm draft) {
    return diskDataStore.add(draft).subscribeOn(Schedulers.io());
  }

  public Completable deleteDraft() {
    return diskDataStore.delete("draft").subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByUserFeed(String cursor, String eTag,
      int limit) {
    return Observable.mergeDelayError(
        diskDataStore.listByUserFeed().subscribeOn(Schedulers.io()),
        remoteDataStore.listByUserFeed(cursor, eTag, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()).subscribe())
            .subscribeOn(Schedulers.io()));
  }

  public Observable<Response<List<PostRealm>>> listByBounty(String cursor, String eTag, int limit) {
    return Observable.mergeDelayError(
        diskDataStore.listByBounty().subscribeOn(Schedulers.io()),
        remoteDataStore.listByBounty(cursor, eTag, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()).subscribe())
            .subscribeOn(Schedulers.io()));
  }

  public Observable<Response<List<PostRealm>>> listByCategory(String categoryName,
      PostSorter sorter, String cursor, String eTag, int limit) {
    Preconditions.checkNotNull(categoryName, "categoryName can not be null.");

    return Observable.mergeDelayError(
        diskDataStore.listByCategory(categoryName, sorter).subscribeOn(Schedulers.io()),
        remoteDataStore.listByCategory(categoryName, sorter, cursor, eTag, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()).subscribe())
            .subscribeOn(Schedulers.io()));
  }

  public Observable<Response<List<PostRealm>>> listByTags(String tagNames, PostSorter sorter,
      String cursor, String eTag, int limit) {
    Preconditions.checkNotNull(tagNames, "tagNames can not be null.");

    return remoteDataStore.listByTags(tagNames, sorter, cursor, eTag, limit)
        .doOnNext(response -> diskDataStore.addAll(response.getData()).subscribe())
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByUser(String userId, boolean commented,
      String cursor, String eTag, int limit) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    return remoteDataStore.listByUser(userId, commented, cursor, eTag, limit)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByBookmarked(String cursor, String eTag,
      int limit) {
    return Observable.mergeDelayError(
        diskDataStore.listBookmarkedPosts().subscribeOn(Schedulers.io()),
        remoteDataStore.listByBookmarked(cursor, eTag, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()).subscribe())
            .subscribeOn(Schedulers.io()));
  }

  public Completable votePost(String postId, int direction) {
    Preconditions.checkNotNull(postId, "postId can not be null.");

    return diskDataStore.vote(postId, direction).subscribeOn(Schedulers.io());
  }

  public Completable bookmarkPost(String postId) {
    Preconditions.checkNotNull(postId, "postId can not be null.");

    return diskDataStore.bookmark(postId).subscribeOn(Schedulers.io());
  }
}