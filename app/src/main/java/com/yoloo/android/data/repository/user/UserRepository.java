package com.yoloo.android.data.repository.user;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.util.Preconditions;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

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

  public Observable<AccountRealm> getUser(String userId) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    // TODO: 24.01.2017 Convert it to remote.
    return diskDataStore.get(userId).subscribeOn(Schedulers.io());
    /*return remoteDataStore.getPost(userId)
        .subscribeOn(Schedulers.io());*/
  }

  public Observable<AccountRealm> getMe() {
    return remoteDataStore.getMe().doOnNext(diskDataStore::add).subscribeOn(Schedulers.io());
  }

  public Observable<AccountRealm> getLocalMe() {
    return diskDataStore.getMe().subscribeOn(Schedulers.io()).cache();
  }

  public Single<AccountRealm> addUser(AccountRealm account) {
    Preconditions.checkNotNull(account, "account can not be null.");

    return remoteDataStore.add(account)
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<AccountRealm>>> searchUser(String query, String cursor,
      int limit) {
    Preconditions.checkNotNull(query, "query can not be null.");

    return remoteDataStore.list(query, cursor, limit).subscribeOn(Schedulers.io());
  }

  public Observable<List<AccountRealm>> listRecentSearchedUsers() {
    return diskDataStore.listRecentSearches().subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<AccountRealm>>> listFollowers(String userId, String cursor,
      String eTag, int limit) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    return remoteDataStore.listFollowers(userId, cursor, eTag, limit).subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<AccountRealm>>> listFollowings(String userId, String cursor,
      String eTag, int limit) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    return remoteDataStore.listFollowings(userId, cursor, eTag, limit).subscribeOn(Schedulers.io());
  }

  public Completable follow(String userId, int direction) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    return remoteDataStore.follow(userId, direction);
  }
}
