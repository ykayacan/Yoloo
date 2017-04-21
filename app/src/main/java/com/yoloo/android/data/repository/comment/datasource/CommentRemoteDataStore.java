package com.yoloo.android.data.repository.comment.datasource;

import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.transformer.CommentResponseTransformer;

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

public class CommentRemoteDataStore {

  private static CommentRemoteDataStore instance;

  private CommentRemoteDataStore() {
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static CommentRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new CommentRemoteDataStore();
    }
    return instance;
  }

  /**
   * Get single.
   *
   * @param postId the post id
   * @param commentId the comment id
   * @return the single
   */
  public Single<CommentRealm> get(@Nonnull String postId, @Nonnull String commentId) {
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .comments()
                    .get(postId, commentId)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(CommentRealm::new);
  }

  /**
   * Add single.
   *
   * @param comment the comment
   * @return the single
   */
  public Single<CommentRealm> add(@Nonnull CommentRealm comment) {
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .comments()
                    .insert(comment.getPostId(), comment.getContent())
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(CommentRealm::new);
  }

  /**
   * Delete completable.
   *
   * @param comment the comment
   * @return the completable
   */
  public Completable delete(@Nonnull CommentRealm comment) {
    return getIdToken()
        .flatMapCompletable(idToken ->
            Completable.fromAction(() ->
                INSTANCE.getApi()
                    .posts()
                    .comments()
                    .delete(comment.getPostId(), comment.getId())
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()));
  }

  /**
   * List observable.
   *
   * @param postId the post id
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<CommentRealm>>> list(@Nonnull String postId,
      @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken ->
            Observable.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .comments()
                    .list(postId)
                    .setCursor(cursor)
                    .setLimit(limit)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .compose(CommentResponseTransformer.create());
  }

  /**
   * Accept single.
   *
   * @param comment the comment
   * @return the single
   */
  public Single<CommentRealm> accept(@Nonnull CommentRealm comment) {
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .posts()
                    .comments()
                    .update(comment.getPostId(), comment.getId())
                    .setAccepted(true)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(CommentRealm::new);
  }

  private HttpHeaders setIdTokenHeader(@Nonnull String idToken) {
    Timber.d("Id Token: %s", idToken);
    return new HttpHeaders().setAuthorization("Bearer " + idToken);
  }
}
