package com.yoloo.android.data.repository.comment.datasource;

import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.FakerUtil;
import com.yoloo.android.data.model.CommentRealm;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.yoloo.android.data.ApiManager.INSTANCE;
import static com.yoloo.android.data.ApiManager.getIdToken;

public class CommentRemoteDataStore {

  private static CommentRemoteDataStore instance;

  private CommentRemoteDataStore() {
  }

  public static CommentRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new CommentRemoteDataStore();
    }
    return instance;
  }

  public Single<CommentRealm> get(@Nonnull String postId, @Nonnull String commentId) {
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi().posts()
                    .comments()
                    .get(postId, commentId)
                    .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(CommentRealm::new);
  }

  public Observable<CommentRealm> add(@Nonnull CommentRealm comment) {
    return Observable.just(comment);
  }

  public Completable delete(@Nonnull CommentRealm comment) {
    getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());

    return Completable.complete();
  }

  public Observable<Response<List<CommentRealm>>> list(String postId, String cursor, int limit) {
    return getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  public Observable<CommentRealm> accept(CommentRealm comment) {
    return getIdToken()
        .toObservable()
        .flatMap(s -> Observable.just(
            new CommentRealm()
                .setId(comment.getId())
                .setOwnerId("a1")
                .setUsername("krialix")
                .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
                .setAccepted(true)
                .setPostId(comment.getPostId())
                .setContent("Accepted")
                .setCreated(new Date())));
  }
}
