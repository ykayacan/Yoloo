package com.yoloo.android.data.repository.user.datasource;

import com.annimon.stream.Stream;
import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;

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

public class UserRemoteDataStore {

  private static UserRemoteDataStore instance;

  private UserRemoteDataStore() {
  }

  public static UserRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new UserRemoteDataStore();
    }
    return instance;
  }

  public Single<AccountRealm> getMe() {
    return getIdToken().doOnSuccess(s -> Timber.d("Token: %s", s))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .accounts()
            .me()
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .map(AccountRealm::new)
        .map(account -> account.setMe(true));
  }

  public Single<AccountRealm> get(@Nonnull String userId) {
    return getIdToken().doOnSuccess(s -> Timber.d("Token: %s", s))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .accounts()
            .get(userId)
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .map(AccountRealm::new);
  }

  public Single<AccountRealm> add(@Nonnull AccountRealm account) {
    return getIdToken().doOnSuccess(s -> Timber.d("IdToken: %s", s))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .accounts()
            .register(account.getLocale(), account.getGender(), account.getRealname(),
                account.getCategoryIds())
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .map(AccountRealm::new)
        .map(__ -> __.setMe(true))
        .map(__ -> __.setCategoryIds(account.getCategoryIds()));
  }

  public Single<AccountRealm> update(@Nonnull AccountRealm account) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .accounts()
            .update(account.getId())
            .setMediaId(account.getAvatarUrl())
            .setUsername(account.getUsername())
            .setName(account.getRealname())
            .setGender(account.getGender().toUpperCase())
            .setWebsiteUrl(account.getWebsiteUrl())
            .setBio(account.getBio())
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .map(AccountRealm::new)
        .map(__ -> __.setMe(account.isMe()))
        .map(__ -> __.setCategoryIds(account.getCategoryIds()));
  }

  public Observable<Response<List<AccountRealm>>> list(@Nonnull String name,
      @Nullable String cursor, int limit) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .accounts()
            .search(name)
            .setCursor(cursor)
            .setLimit(limit)
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .filter(response -> response.getItems() != null)
        .map(response -> Response.create(
            Stream.of(response.getItems()).map(AccountRealm::new).toList(),
            response.getNextPageToken()))
        .toObservable();
  }

  public Observable<Response<List<AccountRealm>>> listFollowers(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .accounts()
            .followers(userId)
            .setCursor(cursor)
            .setLimit(limit)
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .map(response -> Response.create(
            Stream.of(response.getItems()).map(AccountRealm::new).toList(),
            response.getNextPageToken()))
        .toObservable();
  }

  public Observable<Response<List<AccountRealm>>> listFollowings(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .accounts()
            .followings(userId)
            .setCursor(cursor)
            .setLimit(limit)
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .map(response -> Response.create(
            Stream.of(response.getItems()).map(AccountRealm::new).toList(),
            response.getNextPageToken()))
        .toObservable();
  }

  public Completable follow(@Nonnull String userId) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .follows()
            .follow(userId)
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .toCompletable();
  }

  public Completable unfollow(@Nonnull String userId) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken -> Single.fromCallable(() -> INSTANCE.getApi()
            .follows()
            .unfollow(userId)
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()).subscribeOn(Schedulers.io()))
        .toCompletable();
  }
}
