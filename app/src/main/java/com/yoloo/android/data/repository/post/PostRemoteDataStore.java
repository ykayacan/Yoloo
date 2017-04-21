package com.yoloo.android.data.repository.post;

import com.annimon.stream.Stream;
import com.google.api.client.http.HttpHeaders;
import com.squareup.moshi.Moshi;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.UploadManager;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.upload.UploadResponse;
import com.yoloo.android.data.sorter.PostSorter;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import timber.log.Timber;

import static com.yoloo.android.data.ApiManager.INSTANCE;
import static com.yoloo.android.data.ApiManager.getIdToken;

class PostRemoteDataStore {

  private static PostRemoteDataStore instance;

  private PostRemoteDataStore() {
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  static PostRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new PostRemoteDataStore();
    }
    return instance;
  }

  /**
   * Get single.
   *
   * @param postId the post id
   * @return the single
   */
  Single<PostRealm> get(@Nonnull String postId) {
    return getIdToken()
        .flatMap(idToken -> Single
            .fromCallable(() -> INSTANCE
                .getApi()
                .posts()
                .get(postId)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(PostRealm::new);
  }

  /**
   * Add single.
   *
   * @param post the post
   * @return the single
   */
  Single<PostRealm> add(@Nonnull PostRealm post) {
    return getIdToken().flatMap(idToken -> {
      if (post.isQuestion()) {
        if (post.getMedias().isEmpty()) {
          return Single.fromCallable(() -> INSTANCE
              .getApi()
              .questions()
              .insert(post.getContent(), post.getGroupId(), post.getTagNamesAsString())
              .setRequestHeaders(setIdTokenHeader(idToken))
              .execute());
        } else {
          return uploadMedia(post).flatMapSingle(mediaId -> Single.fromCallable(() -> INSTANCE
              .getApi()
              .questions()
              .insert(post.getContent(), post.getGroupId(), post.getTagNamesAsString())
              .setMediaId(mediaId)
              .setRequestHeaders(setIdTokenHeader(idToken))
              .execute()));
        }
      } else if (post.isBlog()) {
        if (post.getMedias().isEmpty()) {
          return Single.fromCallable(() -> INSTANCE
              .getApi()
              .blogs()
              .insert(post.getContent(), post.getTagNamesAsString(), post.getGroupId(),
                  post.getTitle())
              .setRequestHeaders(setIdTokenHeader(idToken))
              .execute());
        } else {
          return uploadMedia(post).flatMapSingle(mediaId -> Single.fromCallable(() -> INSTANCE
              .getApi()
              .blogs()
              .insert(post.getContent(), post.getTagNamesAsString(), post.getGroupId(),
                  post.getTitle())
              .setMediaIds(mediaId)
              .setRequestHeaders(setIdTokenHeader(idToken))
              .execute()));
        }
      }

      throw new IllegalArgumentException("postType is not valid.");
    }).subscribeOn(Schedulers.io()).map(PostRealm::new);
  }

  private Maybe<String> uploadMedia(@Nonnull PostRealm post) {
    List<File> files =
        Stream.of(post.getMedias()).map(MediaRealm::getTempPath).map(File::new).toList();

    return UploadManager.INSTANCE
        .upload(post.getOwnerId(), files)
        .map(response -> response.body().string())
        .map(json -> {
          Moshi moshi = new Moshi.Builder().build();
          return moshi.adapter(UploadResponse.class).fromJson(json);
        })
        .map(response -> response.getItems().get(0).getId())
        .filter(s -> s != null);
  }

  /**
   * Delete completable.
   *
   * @param postId the post id
   * @return the completable
   */
  Completable delete(@Nonnull String postId) {
    return getIdToken().flatMapCompletable(idToken -> Completable
        .fromAction(() -> INSTANCE
            .getApi()
            .posts()
            .delete(postId)
            .setRequestHeaders(setIdTokenHeader(idToken))
            .execute())
        .subscribeOn(Schedulers.io()));
  }

  /**
   * List by feed observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<PostRealm>>> listByFeed(@Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .me()
                .feed()
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(PostResponseTransformer.create());
  }

  /**
   * List by bounty observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<PostRealm>>> listByBounty(@Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .questions()
                .list()
                .setSort("BOUNTY")
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .filter(response -> response.getItems() != null)
        .compose(PostResponseTransformer.create());
  }

  /**
   * List by category observable.
   *
   * @param groupId the category name
   * @param sorter the sorter
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<PostRealm>>> listByGroup(@Nonnull String groupId,
      @Nonnull PostSorter sorter, @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .questions()
                .list()
                .setCategory(groupId)
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(PostResponseTransformer.create());
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
  Observable<Response<List<PostRealm>>> listByTags(@Nonnull String tagNames,
      @Nonnull PostSorter sorter, @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .questions()
                .list()
                .setTags(tagNames)
                .setSort(sorter.name())
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(PostResponseTransformer.create());
  }

  /**
   * List by bookmarked observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<PostRealm>>> listByBookmarked(@Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .posts()
                .list()
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(PostResponseTransformer.create());
  }

  /**
   * List by user observable.
   *
   * @param userId the user id
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<PostRealm>>> listByUser(@Nullable String userId, @Nullable String cursor,
      int limit) {
    return Observable.empty();
    /*return getIdToken().flatMapObservable(idToken -> Observable.fromCallable(() -> INSTANCE.getApi()
        .posts()
        .list()
        .setUserId(userId)
        .setCursor(cursor)
        .setLimit(limit)
        .setRequestHeaders(setIdTokenHeader(idToken))
        .execute()).subscribeOn(Schedulers.io())).compose(UserResponseTransformer.create());*/
  }

  /**
   * List by trending blogs observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<PostRealm>>> listByTrendingBlogs(@Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .blogs()
                .list()
                .setSort("HOT")
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(PostResponseTransformer.create());
  }

  /**
   * Vote completable.
   *
   * @param postId the post id
   * @param direction the direction
   * @return the completable
   */
  Completable vote(@Nonnull String postId, int direction) {
    return getIdToken().flatMapCompletable(idToken -> Completable
        .fromCallable(() -> INSTANCE
            .getApi()
            .posts()
            .vote(postId, direction)
            .setRequestHeaders(setIdTokenHeader(idToken))
            .execute())
        .subscribeOn(Schedulers.io()));
  }

  /**
   * Bookmark completable.
   *
   * @param postId the post id
   * @return the completable
   */
  Completable bookmark(@Nonnull String postId) {
    return getIdToken().flatMapCompletable(idToken -> Completable
        .fromCallable(() -> INSTANCE
            .getApi()
            .posts()
            .bookmark(postId)
            .setRequestHeaders(setIdTokenHeader(idToken))
            .execute())
        .subscribeOn(Schedulers.io()));
  }

  /**
   * Unbookmark completable.
   *
   * @param postId the post id
   * @return the completable
   */
  Completable unbookmark(@Nonnull String postId) {
    return getIdToken().flatMapCompletable(idToken -> Completable
        .fromCallable(() -> INSTANCE
            .getApi()
            .posts()
            .unbookmark(postId)
            .setRequestHeaders(setIdTokenHeader(idToken))
            .execute())
        .subscribeOn(Schedulers.io()));
  }

  private HttpHeaders setIdTokenHeader(@Nonnull String idToken) {
    Timber.d("Id Token: %s", idToken);
    return new HttpHeaders().setAuthorization("Bearer " + idToken);
  }
}
