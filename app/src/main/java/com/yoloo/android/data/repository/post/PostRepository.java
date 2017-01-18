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

  /**
   * Gets instance.
   *
   * @param remoteDataStore the remote data store
   * @param diskDataStore the disk data store
   * @return the instance
   */
  public static PostRepository getInstance(PostRemoteDataStore remoteDataStore,
      PostDiskDataStore diskDataStore) {
    if (INSTANCE == null) {
      INSTANCE = new PostRepository(remoteDataStore, diskDataStore);
    }
    return INSTANCE;
  }

  /**
   * Get observable.
   *
   * @param postId the post id
   * @return the observable
   */
  public Observable<PostRealm> get(String postId) {
    Preconditions.checkNotNull(postId, "postId can not be null.");

    return Observable.mergeDelayError(
        diskDataStore.get(postId),
        remoteDataStore.get(postId)
            .subscribeOn(Schedulers.io())
            .doOnNext(diskDataStore::add));
  }

  /**
   * Add observable.
   *
   * @param post the post realm
   * @return the observable
   */
  public Observable<PostRealm> add(PostRealm post) {
    Preconditions.checkNotNull(post, "post can not be null.");

    return remoteDataStore.add(post)
        .subscribeOn(Schedulers.io())
        .doOnNext(diskDataStore::add);
  }

  /**
   * Delete.
   *
   * @param postId the post id
   */
  public Completable delete(String postId) {
    Preconditions.checkNotNull(postId, "postId can not be null.");

    return Completable.fromAction(() -> {
      diskDataStore.delete(postId);
      remoteDataStore.delete(postId);
    });
  }

  /**
   * List observable.
   *
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit
   * @param sorter the sorter
   * @param category the category
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> list(String cursor, String eTag, int limit,
      PostSorter sorter, String category) {
    return Observable.mergeDelayError(
        diskDataStore.list(category),
        remoteDataStore.list(sorter, category, cursor, eTag, limit)
            .subscribeOn(Schedulers.io())
            .doOnNext(response -> diskDataStore.addAll(response.getData())));
  }

  /**
   * List feed observable.
   *
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listFeed(String cursor, String eTag, int limit) {
    return Observable.mergeDelayError(
        diskDataStore.list(null),
        remoteDataStore.listFeed(cursor, eTag, limit)
            .subscribeOn(Schedulers.io())
            .filter(listResponse -> !listResponse.getData().isEmpty())
            .doOnNext(response -> diskDataStore.addAll(response.getData())));
  }

  /**
   * Vote completable.
   *
   * @param postId the post id
   * @param direction the direction
   * @return the completable
   */
  public Completable vote(String postId, int direction) {
    return Completable.fromAction(() -> {
      diskDataStore.vote(postId, direction);
    });
  }
}