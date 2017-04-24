package com.yoloo.android.data.repository.user;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.api.client.util.Base64;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.AccountFaker;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GameInfoRealm;
import com.yoloo.android.data.model.RegisterUserPayload;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import timber.log.Timber;

public class UserRepository {

  private static UserRepository instance;

  private final UserRemoteDataStore remoteDataStore;
  private final UserDiskDataStore diskDataStore;

  private UserRepository(UserRemoteDataStore remoteDataStore, UserDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static UserRepository getInstance(UserRemoteDataStore remoteDataStore,
      UserDiskDataStore diskDataStore) {
    if (instance == null) {
      instance = new UserRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  public Observable<AccountRealm> getUser(@Nonnull String userId) {
    Observable<AccountRealm> remoteObservable =
        remoteDataStore.get(userId).subscribeOn(Schedulers.io()).map(account -> {
          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
          if (user != null && user.getEmail() != null) {
            account.setMe(user.getEmail().equals(account.getEmail()));
          }

          return account;
        }).doOnSuccess(account -> {
          if (account.isMe()) {
            diskDataStore.add(account);
          }
        }).toObservable();

    Observable<AccountRealm> diskObservable = diskDataStore
        .get(userId)
        .subscribeOn(Schedulers.io())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toObservable();

    return Observable.mergeDelayError(diskObservable, remoteObservable);
  }

  public Observable<AccountRealm> getMe() {
    Observable<AccountRealm> diskObservable = diskDataStore
        .getMe()
        .subscribeOn(Schedulers.io())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toObservable();

    Observable<AccountRealm> remoteObservable = remoteDataStore
        .getMe()
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io())
        .toObservable();

    return Observable.mergeDelayError(diskObservable, remoteObservable);
  }

  public Single<AccountRealm> getLocalMe() {
    return diskDataStore
        .getMe()
        .subscribeOn(Schedulers.io())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toSingle();
  }

  public Single<AccountRealm> registerUser(@Nonnull AccountRealm account) {
    RegisterUserPayload payload = new RegisterUserPayload(account);

    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<RegisterUserPayload> jsonAdapter = moshi.adapter(RegisterUserPayload.class);

    String json = jsonAdapter.toJson(payload);
    Timber.d("Payload: %s", json);
    String base64Payload = Base64.encodeBase64URLSafeString(json.getBytes());

    return remoteDataStore
        .add(base64Payload)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  public Single<AccountRealm> updateMe(@Nonnull AccountRealm account) {
    return remoteDataStore
        .update(account)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<AccountRealm>>> searchUser(@Nonnull String query,
      @Nullable String cursor, int limit) {
    return remoteDataStore.list(query, cursor, limit).subscribeOn(Schedulers.io());
  }

  public Single<Boolean> checkUsername(@Nonnull String username) {
    return remoteDataStore.checkUsername(username);
  }

  public Observable<Response<List<AccountRealm>>> listNewUsers(@Nullable String cursor, int limit) {
    return Observable.just(
        Response.create(Stream.range(0, 6).map(__ -> AccountFaker.generateOne()).toList(), null));
  }

  public Observable<List<AccountRealm>> listRecentSearchedUsers() {
    return diskDataStore.listRecentSearches().subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<AccountRealm>>> listFollowers(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return remoteDataStore.listFollowers(userId, cursor, limit).subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<AccountRealm>>> listFollowings(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return remoteDataStore.listFollowings(userId, cursor, limit).subscribeOn(Schedulers.io());
  }

  public Single<GameInfoRealm> getGameInfo() {
    return remoteDataStore.getGameInfo();
  }

  public Completable relationship(@Nonnull String userId, int direction) {
    return remoteDataStore.relationship(userId, direction == 1 ? "FOLLOW" : "UNFOLLOW");
  }
}
