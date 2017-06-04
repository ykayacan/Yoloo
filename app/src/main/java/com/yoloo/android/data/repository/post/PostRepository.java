package com.yoloo.android.data.repository.post;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.util.NetworkUtil;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.net.SocketTimeoutException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostRepository {

  private static PostRepository instance;

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
    if (instance == null) {
      instance = new PostRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  /**
   * Gets post.
   *
   * @param postId the post id
   * @return the post
   */
  public Single<PostRealm> getPost(@Nonnull String postId) {
    Single<PostRealm> remoteObservable =
        remoteDataStore.get(postId).doOnSuccess(diskDataStore::add).subscribeOn(Schedulers.io());

    Single<Optional<PostRealm>> diskObservable =
        diskDataStore.get(postId).subscribeOn(Schedulers.io());

    return diskObservable.flatMap(
        postOptional -> postOptional.isPresent() ? Single.just(postOptional.get())
            : remoteObservable);
  }

  /**
   * Add post single.
   *
   * @param post the post
   * @return the single
   */
  public Single<PostRealm> addPost(@Nonnull PostRealm post) {
    return remoteDataStore.add(post).doOnSuccess(diskDataStore::add).subscribeOn(Schedulers.io());
  }

  /**
   * Delete post completable.
   *
   * @param postId the post id
   * @return the completable
   */
  public Completable deletePost(@Nonnull String postId) {
    return remoteDataStore.delete(postId)
        .andThen(diskDataStore.delete(postId))
        .subscribeOn(Schedulers.io());
  }

  /**
   * Gets draft.
   *
   * @return the draft
   */
  public Single<PostRealm> getDraft() {
    return diskDataStore.get("draft")
        .subscribeOn(Schedulers.io())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toSingle();
  }

  /**
   * Add draft completable.
   *
   * @param draft the draft
   * @return the completable
   */
  public Completable addDraft(@Nonnull PostRealm draft) {
    return Completable.fromAction(() -> diskDataStore.add(draft)).subscribeOn(Schedulers.io());
  }

  /**
   * Delete draft completable.
   *
   * @return the completable
   */
  public Completable deleteDraft() {
    return diskDataStore.delete("draft").subscribeOn(Schedulers.io());
  }

  /**
   * List by feed observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listByFeed(@Nullable String cursor, int limit) {
    Observable<Response<List<PostRealm>>> remoteObservable =
        remoteDataStore.listByFeed(cursor, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()))
            .subscribeOn(Schedulers.io());

    Observable<Response<List<PostRealm>>> diskObservable =
        diskDataStore.listByFeed().subscribeOn(Schedulers.io());

    return NetworkUtil.isNetworkAvailable(YolooApp.getAppContext()) ? remoteObservable
        : diskObservable;
  }

  /**
   * List by bounty observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listByBounty(@Nullable String cursor, int limit) {
    return remoteDataStore.listByBounty(cursor, limit).subscribeOn(Schedulers.io());
  }

  /**
   * List by group observable.
   *
   * @param groupId the group id
   * @param sorter the sorter
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listByGroup(@Nonnull String groupId,
      @Nonnull PostSorter sorter, @Nullable String cursor, int limit) {
    return remoteDataStore.listByGroup(groupId, sorter, cursor, limit)
        .subscribeOn(Schedulers.io())
        .retry(throwable -> throwable instanceof SocketTimeoutException);
  }

  /**
   * List by tags observable.
   *
   * @param tagNames the tag names
   * @param sorter the sorter
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listByTags(@Nonnull String tagNames,
      @Nonnull PostSorter sorter, @Nullable String cursor, int limit) {
    return remoteDataStore.listByTags(tagNames, sorter, cursor, limit).subscribeOn(Schedulers.io());
  }

  /**
   * List by user observable.
   *
   * @param userId the user id
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listByUser(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return remoteDataStore.listByUser(userId, cursor, limit).subscribeOn(Schedulers.io());
  }

  /**
   * List by bookmarked observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listByBookmarked(@Nullable String cursor,
      int limit) {

    Observable<Response<List<PostRealm>>> remoteObservable =
        remoteDataStore.listByBookmarked(cursor, limit)
            .doOnNext(response -> diskDataStore.addAll(Stream.of(response.getData())
                .map(postRealm -> postRealm.setBookmarked(true))
                .toList()))
            .subscribeOn(Schedulers.io());

    Observable<Response<List<PostRealm>>> diskObservable = diskDataStore.listByBookmarkedPosts();

    return Observable.merge(remoteObservable, diskObservable);
  }

  /**
   * List by post sorter observable.
   *
   * @param sorter the sorter
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listByPostSorter(@Nonnull PostSorter sorter,
      @Nullable String cursor, int limit) {
    return remoteDataStore.listByPostSorter(sorter, cursor, limit).subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<PostRealm>>> listByTrendingBlogPosts(@Nullable String cursor,
      int limit) {
    Observable<Response<List<PostRealm>>> diskObservable =
        diskDataStore.listByTrendingBlogPosts(limit)
            .doOnNext(response -> diskDataStore.addTrendingBlogs(response.getData()))
            .subscribeOn(Schedulers.io());

    Observable<Response<List<PostRealm>>> remoteObservable =
        remoteDataStore.listByTrendingBlogPosts(cursor, limit)
            .doOnNext(response -> diskDataStore.addTrendingBlogs(response.getData()))
            .subscribeOn(Schedulers.io());

    return diskObservable.flatMap(response -> {
      if (response.getData().isEmpty()) {
        return remoteObservable;
      }

      return Observable.just(response);
    });
  }

  public Observable<Response<List<PostRealm>>> listByMediaPosts(@Nullable String cursor,
      int limit) {
    return remoteDataStore.listByMediaPosts(cursor, limit).subscribeOn(Schedulers.io());
  }

  /**
   * Vote post completable.
   *
   * @param postId the post id
   * @param direction the direction
   * @return the completable
   */
  public Single<PostRealm> votePost(@Nonnull String postId, int direction) {
    return remoteDataStore.vote(postId, direction)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  /**
   * Bookmark post completable.
   *
   * @param postId the post id
   * @return the completable
   */
  public Single<PostRealm> bookmarkPost(@Nonnull String postId) {
    return remoteDataStore.bookmark(postId)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  /**
   * Un bookmark post completable.
   *
   * @param postId the post id
   * @return the completable
   */
  public Single<PostRealm> unBookmarkPost(@Nonnull String postId) {
    return remoteDataStore.unbookmark(postId)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }
}
