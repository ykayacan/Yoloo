package com.yoloo.android.data.repository.user.datasource;

import com.annimon.stream.Stream;
import com.google.api.client.http.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.UploadManager;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.upload.UploadResponse;
import com.yoloo.android.util.StringUtil;
import com.yoloo.backend.yolooApi.YolooApi;
import com.yoloo.backend.yolooApi.model.AccountDTO;
import com.yoloo.backend.yolooApi.model.WrappedBoolean;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .users()
                    .me()
                    .get()
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(AccountRealm::new)
        .map(account -> account.setMe(true));
  }

  public Single<AccountRealm> get(@Nonnull String userId) {
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .users()
                    .get(userId)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(AccountRealm::new);
  }

  public Single<AccountRealm> add(@Nonnull AccountRealm account) {
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .users()
                    .register(account.getLocale(), account.getGender(), account.getRealname(),
                        account.getCategoryIds())
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(AccountRealm::new)
        .map(__ -> __.setMe(true))
        .map(__ -> __.setCategoryIds(account.getCategoryIds()));
  }

  public Single<AccountRealm> update(@Nonnull AccountRealm account) {
    return getIdToken()
        .flatMap(idToken -> {
          String avatarUrl = account.getAvatarUrl();
          if (!StringUtil.isNullOrEmpty(avatarUrl)) {
            List<File> files = Collections.singletonList(new File(avatarUrl));

            return UploadManager.getInstance()
                .upload(account.getId(), files)
                .map(response -> response.body().string())
                .map(json -> {
                  Moshi moshi = new Moshi.Builder().build();
                  JsonAdapter<UploadResponse> jsonAdapter = moshi.adapter(UploadResponse.class);
                  return jsonAdapter.fromJson(json);
                })
                .doOnSuccess(response -> account.setAvatarUrl(response.getItems().get(0).getId()))
                .flatMap(uploadResponse -> Single.fromCallable(() -> updateUser(account, idToken)))
                .subscribeOn(Schedulers.io());
          }

          return Single.fromCallable(() -> updateUser(account, idToken))
              .subscribeOn(Schedulers.io());
        })
        .map(AccountRealm::new)
        .map(__ -> __.setMe(account.isMe()))
        .map(__ -> __.setCategoryIds(account.getCategoryIds()));
  }

  public Observable<Response<List<AccountRealm>>> list(@Nonnull String name,
      @Nullable String cursor, int limit) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .users()
                    .search(name)
                    .setCursor(cursor)
                    .setLimit(limit)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .filter(response -> response.getItems() != null)
        .map(response -> Response.create(
            Stream.of(response.getItems()).map(AccountRealm::new).toList(),
            response.getNextPageToken()))
        .toObservable();
  }

  public Observable<Response<List<AccountRealm>>> listFollowers(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .users()
                    .followedBy(userId)
                    .setCursor(cursor)
                    .setLimit(limit)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute()).subscribeOn(Schedulers.io()))
        .map(response ->
            Response.create(Stream.of(response.getItems()).map(AccountRealm::new).toList(),
                response.getNextPageToken()))
        .toObservable();
  }

  public Observable<Response<List<AccountRealm>>> listFollowings(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .users()
                    .follows(userId)
                    .setCursor(cursor)
                    .setLimit(limit)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(response -> Response.create(
            Stream.of(response.getItems()).map(AccountRealm::new).toList(),
            response.getNextPageToken()))
        .toObservable();
  }

  public Completable relationship(@Nonnull String userId, @Nonnull String action) {
    return getIdToken().doOnSuccess(idToken -> Timber.d("Token: %s", idToken))
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .users()
                    .relationship(userId, action)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .toCompletable();
  }

  public Single<Boolean> checkUsername(@Nonnull String username) {
    return getIdToken()
        .flatMap(idToken ->
            Single.fromCallable(() ->
                INSTANCE.getApi()
                    .users()
                    .checkUsername(username)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute())
                .subscribeOn(Schedulers.io()))
        .map(WrappedBoolean::getAvailable);
  }

  private HttpHeaders setIdTokenHeader(@Nonnull String idToken) {
    Timber.d("ID TOKEN: %s", idToken);
    return new HttpHeaders().setAuthorization("Bearer " + idToken);
  }

  private AccountDTO updateUser(AccountRealm account, String idToken) throws IOException {
    YolooApi.Users.Me.Update update = INSTANCE.getApi().users().me().update();

    String mediaId = account.getAvatarUrl();
    if (!StringUtil.isNullOrEmpty(mediaId)) {
      update.setMediaId(mediaId);
    }

    String realName = account.getRealname();
    if (!StringUtil.isNullOrEmpty(realName)) {
      update.setName(realName);
    }

    String username = account.getUsername();
    if (!StringUtil.isNullOrEmpty(username)) {
      update.setUsername(username);
    }

    String email = account.getEmail();
    if (!StringUtil.isNullOrEmpty(email)) {
      update.setEmail(email);
      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      if (user != null) {
        user.updateEmail(email);
      }
    }

    String password = account.getPassword();
    if (!StringUtil.isNullOrEmpty(password)) {
      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      if (user != null) {
        user.updatePassword(password);
      }
    }

    String gender = account.getGender();
    if (!StringUtil.isNullOrEmpty(gender)) {
      update.setGender(gender);
    }

    String websiteUrl = account.getWebsiteUrl();
    if (!StringUtil.isNullOrEmpty(websiteUrl)) {
      update.setWebsiteUrl(websiteUrl);
    }

    String bio = account.getBio();
    if (!StringUtil.isNullOrEmpty(bio)) {
      update.setBio(bio);
    }

    return update.setRequestHeaders(setIdTokenHeader(idToken)).execute();
  }
}
