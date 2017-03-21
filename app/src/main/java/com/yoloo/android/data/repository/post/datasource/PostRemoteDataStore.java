package com.yoloo.android.data.repository.post.datasource;

import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.transformer.PostResponseTransformer;
import com.yoloo.android.data.sorter.PostSorter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.yoloo.android.data.ApiManager.INSTANCE;
import static com.yoloo.android.data.ApiManager.getIdToken;

public class PostRemoteDataStore {

  private static PostRemoteDataStore instance;

  private PostRemoteDataStore() {
  }

  public static PostRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new PostRemoteDataStore();
    }
    return instance;
  }

  public Single<PostRealm> get(@Nonnull String postId) {
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .get(postId)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(PostRealm::new);
  }

  public Single<PostRealm> add(@Nonnull PostRealm post) {
    return getIdToken()
        .doOnSuccess(s -> Timber.d("IdToken: %s", s))
        .flatMap(idToken ->
            Single.fromCallable(() -> {
              if (post.getPostType() == PostRealm.POST_TEXT
                  || post.getPostType() == PostRealm.POST_RICH) {
                return INSTANCE.getApi().questions()
                    .insert(post.getContent(), post.getCategoriesAsString(), post.getTagsAsString())
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute();
              } else if (post.getPostType() == PostRealm.POST_BLOG) {
                return INSTANCE.getApi().blogs()
                    .insert(post.getContent(), post.getTagsAsString(), post.getCategoriesAsString(),
                        post.getTitle())
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute();
              }

              throw new IllegalArgumentException("postType is not valid.");
            }).subscribeOn(Schedulers.io()))
        .map(PostRealm::new);
  }

  public Completable delete(@Nonnull String postId) {
    return getIdToken()
        .flatMapCompletable(idToken ->
            Completable.fromAction(() ->
                INSTANCE.getApi()
                    .posts()
                    .delete(postId)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()));
  }

  public Observable<Response<List<PostRealm>>> listByFeed(@Nullable String cursor, int limit) {
    return getIdToken()
        .doOnSuccess(s -> Timber.d("IdToken: %s", s))
        .flatMapObservable(idToken ->
            Observable.fromCallable(() ->
                INSTANCE.getApi()
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

  public Observable<Response<List<PostRealm>>> listByBounty(@Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken ->
            Observable.fromCallable(() ->
                INSTANCE.getApi()
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

  public Observable<Response<List<PostRealm>>> listByCategory(@Nonnull String categoryName,
      @Nonnull PostSorter sorter, @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken ->
            Observable.fromCallable(() ->
                INSTANCE.getApi()
                    .questions()
                    .list()
                    .setCategory(categoryName)
                    .setSort(sorter.name())
                    .setCursor(cursor)
                    .setLimit(limit)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .compose(PostResponseTransformer.create());
  }

  public Observable<Response<List<PostRealm>>> listByTags(@Nonnull String tagNames,
      @Nonnull PostSorter sorter, @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken ->
            Observable.fromCallable(() ->
                INSTANCE.getApi()
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

  public Observable<Response<List<PostRealm>>> listByBookmarked(@Nullable String cursor,
      int limit) {
    return getIdToken()
        .flatMapObservable(idToken ->
            Observable.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .list()
                    .setCursor(cursor)
                    .setLimit(limit)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .compose(PostResponseTransformer.create());
  }

  public Observable<Response<List<PostRealm>>> listByUser(@Nullable String userId,
      @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken ->
            Observable.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .list()
                    .setUserId(userId)
                    .setCursor(cursor)
                    .setLimit(limit)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .compose(PostResponseTransformer.create());
  }

  public Completable vote(@Nonnull String postId, int direction) {
    return getIdToken()
        .flatMapCompletable(idToken ->
            Completable.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .vote(postId, String.valueOf(direction))
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()));
  }

  public Completable bookmark(@Nonnull String postId) {
    return getIdToken()
        .flatMapCompletable(idToken ->
            Completable.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .bookmark(postId)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()));
  }

  public Completable unbookmark(@Nonnull String postId) {
    return getIdToken()
        .flatMapCompletable(idToken ->
            Completable.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .unbookmark(postId)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()));
  }

  private HttpHeaders setIdTokenHeader(@Nonnull String idToken) {
    return new HttpHeaders().setAuthorization("Bearer " + idToken);
  }
}
