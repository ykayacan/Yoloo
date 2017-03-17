package com.yoloo.android.data.repository.user;

import com.annimon.stream.Stream;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.AccountFaker;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

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

  public Single<AccountRealm> getUser(@Nonnull String userId) {
    return remoteDataStore.get(userId)
        .subscribeOn(Schedulers.io())
        .map(account -> {
          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
          if (user != null && user.getEmail() != null) {
            account.setMe(user.getEmail().equals(account.getEmail()));
          }

          return account;
        });
  }

  public Single<AccountRealm> getMe() {
    return remoteDataStore.getMe().doOnSuccess(diskDataStore::add).subscribeOn(Schedulers.io());
  }

  public Single<AccountRealm> getLocalMe() {
    return diskDataStore.getMe().subscribeOn(Schedulers.io());
  }

  public Single<AccountRealm> addUser(@Nonnull AccountRealm account) {
    return remoteDataStore.add(account)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  public Single<AccountRealm> updateUser(@Nonnull AccountRealm account) {
    return remoteDataStore.add(account)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<AccountRealm>>> searchUser(@Nonnull String query,
      @Nullable String cursor, int limit) {
    return remoteDataStore.list(query, cursor, limit).subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<AccountRealm>>> listNewUsers(@Nullable String cursor, int limit) {
    return Observable.just(Response.create(Stream.range(0, 6)
        .map(__ -> AccountFaker.generateOne()).toList(), null));
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

  public Completable follow(@Nonnull String userId, int direction) {
    return direction == 1 ? remoteDataStore.follow(userId) : remoteDataStore.unfollow(userId);
  }
}
