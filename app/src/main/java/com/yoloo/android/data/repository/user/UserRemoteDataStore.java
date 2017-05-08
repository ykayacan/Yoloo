package com.yoloo.android.data.repository.user;

import com.annimon.stream.Stream;
import com.google.api.client.http.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.moshi.Moshi;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.UploadManager;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CountryRealm;
import com.yoloo.android.data.model.GameInfoRealm;
import com.yoloo.android.data.model.upload.UploadResponse;
import com.yoloo.android.util.StringUtil;
import com.yoloo.backend.yolooApi.YolooApi;
import com.yoloo.backend.yolooApi.model.AccountDTO;
import com.yoloo.backend.yolooApi.model.WrappedBoolean;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import timber.log.Timber;

import static com.yoloo.android.data.ApiManager.INSTANCE;
import static com.yoloo.android.data.ApiManager.getIdToken;

class UserRemoteDataStore {

  private static UserRemoteDataStore instance;

  private UserRemoteDataStore() {
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  static UserRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new UserRemoteDataStore();
    }
    return instance;
  }

  /**
   * Gets me.
   *
   * @return the me
   */
  Single<AccountRealm> getMe() {
    return getIdToken()
        .flatMap(idToken -> Single
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .me()
                .get()
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(AccountRealm::new)
        .map(account -> account.setMe(true));
  }

  /**
   * Get single.
   *
   * @param userId the user id
   * @return the single
   */
  Single<AccountRealm> get(@Nonnull String userId) {
    return getIdToken()
        .flatMap(idToken -> Single
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .get(userId)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(AccountRealm::new);
  }

  /**
   * Add single.
   *
   * @param base64Payload the base 64 payload
   * @return the single
   */
  Single<AccountRealm> add(@Nonnull String base64Payload) {
    return Single
        .fromCallable(() -> INSTANCE
            .getApi()
            .users()
            .register()
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + base64Payload))
            .execute())
        .subscribeOn(Schedulers.io())
        .map(AccountRealm::new)
        .map(__ -> __.setMe(true));
  }

  /**
   * Update single.
   *
   * @param account the account
   * @return the single
   */
  Single<AccountRealm> update(@Nonnull AccountRealm account) {
    return getIdToken()
        .flatMap(idToken -> {
          String avatarUrl = account.getAvatarUrl();
          if (!StringUtil.isNullOrEmpty(avatarUrl)) {
            List<File> files = Collections.singletonList(new File(avatarUrl));

            return UploadManager.INSTANCE
                .upload(account.getId(), files, UploadManager.MediaOrigin.PROFILE)
                .map(response -> response.body().string())
                .doOnSuccess(s -> Timber.d("Response: %s", s))
                .map(json -> new Moshi.Builder().build()
                    .adapter(UploadResponse.class)
                    .fromJson(json))
                .map(response -> account.setAvatarUrl(response.getItems().get(0).getId()))
                .flatMap(updated -> Single.fromCallable(() -> updateUser(updated, idToken))
                    .subscribeOn(Schedulers.io()))
                .subscribeOn(Schedulers.io());
          }

          return Single
              .fromCallable(() -> updateUser(account, idToken))
              .subscribeOn(Schedulers.io());
        })
        .map(AccountRealm::new)
        .doOnSuccess(accountRealm -> Timber.d("Updated: %s", accountRealm))
        .map(__ -> __.setMe(account.isMe()))
        .map(__ -> __.setSubscribedGroupIds(account.getSubscribedGroupIds()));
  }

  /**
   * Search user observable.
   *
   * @param query the name
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<AccountRealm>>> searchUser(@Nonnull String query,
      @Nullable String cursor,
      int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .search(query)
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(UserResponseTransformer.create());
  }

  /**
   * List followers observable.
   *
   * @param userId the user id
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<AccountRealm>>> listFollowers(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .followedBy(userId)
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(UserResponseTransformer.create());
  }

  /**
   * List followings observable.
   *
   * @param userId the user id
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<AccountRealm>>> listFollowings(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .follows(userId)
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(UserResponseTransformer.create());
  }

  /**
   * List recommended users observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<AccountRealm>>> listRecommendedUsers(@Nonnull String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .listRecommendedUsers()
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(UserResponseTransformer.create());
  }

  Observable<List<CountryRealm>> listVisitedCountries(@Nonnull String userId) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .visited(userId)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(collection -> {
          if (collection == null) {
            return Collections.emptyList();
          }

          return Stream.of(collection.getItems()).map(CountryRealm::new).toList();
        });
  }

  /**
   * List new users observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<AccountRealm>>> listNewUsers(@Nonnull String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .listNewUsers()
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(UserResponseTransformer.create());
  }

  /**
   * Relationship completable.
   *
   * @param userId the user id
   * @param action the action
   * @return the completable
   */
  Completable relationship(@Nonnull String userId, @Nonnull String action) {
    return getIdToken().flatMapCompletable(idToken -> Completable
        .fromAction(() -> INSTANCE
            .getApi()
            .users()
            .relationship(userId, action)
            .setRequestHeaders(setIdTokenHeader(idToken))
            .execute())
        .subscribeOn(Schedulers.io()));
  }

  /**
   * Check username single.
   *
   * @param username the username
   * @return the single
   */
  Single<Boolean> checkUsername(@Nonnull String username) {
    return Single
        .fromCallable(() -> INSTANCE.getApi().users().checkUsername(username).execute())
        .subscribeOn(Schedulers.io())
        .map(WrappedBoolean::getAvailable);
  }

  /**
   * Check email single.
   *
   * @param email the email
   * @return the single
   */
  Single<Boolean> checkEmail(@Nonnull String email) {
    return Single
        .fromCallable(() -> INSTANCE.getApi().users().checkEmail(email).execute())
        .subscribeOn(Schedulers.io())
        .map(WrappedBoolean::getAvailable);
  }

  /**
   * Gets game info.
   *
   * @return the game info
   */
  Single<GameInfoRealm> getGameInfo() {
    return getIdToken()
        .flatMap(idToken -> Single
            .fromCallable(() -> INSTANCE
                .getApi()
                .users()
                .me()
                .getGameInfo()
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(GameInfoRealm::new);
  }

  Completable removeVisitedCountry(@Nonnull String countyCode) {
    return getIdToken().flatMapCompletable(idToken -> Completable
        .fromAction(() -> INSTANCE
            .getApi()
            .users()
            .me()
            .visited(countyCode)
            .setRequestHeaders(setIdTokenHeader(idToken))
            .execute())
        .subscribeOn(Schedulers.io()));
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

    List<CountryRealm> visitedCountries = account.getVisitedCountries();
    if (!visitedCountries.isEmpty()) {
      update.setVisitedCountryCode(visitedCountries.get(0).getCode());
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
      update.setUrl(websiteUrl);
    }

    String bio = account.getBio();
    if (!StringUtil.isNullOrEmpty(bio)) {
      update.setBio(bio);
    }

    Timber.d("Lat call before update: %s", account);

    return update.setRequestHeaders(setIdTokenHeader(idToken)).execute();
  }
}
