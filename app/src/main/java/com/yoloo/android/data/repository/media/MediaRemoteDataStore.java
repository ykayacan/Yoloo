package com.yoloo.android.data.repository.media;

import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.MediaRealm;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.yoloo.android.data.ApiManager.INSTANCE;
import static com.yoloo.android.data.ApiManager.getIdToken;

class MediaRemoteDataStore {

  private static MediaRemoteDataStore instance;

  private MediaRemoteDataStore() {
  }

  static MediaRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new MediaRemoteDataStore();
    }
    return instance;
  }

  Single<MediaRealm> get(@Nonnull String mediaId) {
    return getIdToken()
        .flatMap(idToken -> Single
            .fromCallable(() -> INSTANCE
                .getApi()
                .medias()
                .get(mediaId)
                .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(MediaRealm::new);
  }

  Observable<CommentRealm> add(@Nonnull CommentRealm comment) {
    return Observable.just(comment);
  }

  Completable delete(@Nonnull CommentRealm comment) {
    getIdToken().toObservable().flatMap(s -> Observable.empty());

    return Completable.complete();
  }

  Observable<Response<List<MediaRealm>>> list(@Nonnull String userId, @Nullable String cursor,
      int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .medias()
                .list(userId)
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(MediaResponseTransformer.create());
  }

  Observable<Response<List<MediaRealm>>> listRecentMedias(@Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .medias()
                .listRecentMedias()
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(MediaResponseTransformer.create());
  }

  private HttpHeaders setIdTokenHeader(@Nonnull String idToken) {
    return new HttpHeaders().setAuthorization("Bearer " + idToken);
  }
}
