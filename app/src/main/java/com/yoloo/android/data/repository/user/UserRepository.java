package com.yoloo.android.data.repository.user;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.util.Preconditions;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class UserRepository {

  private static UserRepository INSTANCE;

  private final UserRemoteDataStore remoteDataStore;
  private final UserDiskDataStore diskDataStore;

  private UserRepository(UserRemoteDataStore remoteDataStore, UserDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static UserRepository getInstance(UserRemoteDataStore remoteDataStore,
      UserDiskDataStore diskDataStore) {
    if (INSTANCE == null) {
      INSTANCE = new UserRepository(remoteDataStore, diskDataStore);
    }
    return INSTANCE;
  }

  /**
   * Get single.
   *
   * @param userId the user id
   * @return the single
   */
  public Single<AccountRealm> get(String userId) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    return remoteDataStore.get(userId)
        .subscribeOn(Schedulers.io())
        .doOnSuccess(diskDataStore::add);
  }

  /**
   * Gets me.
   *
   * @return the me
   */
  public Single<AccountRealm> getMe() {
    return remoteDataStore.getMe()
        .subscribeOn(Schedulers.io())
        .doOnSuccess(diskDataStore::add);
  }

  /**
   * Add single.
   *
   * @param account the account
   * @return the single
   */
  public Single<AccountRealm> add(AccountRealm account) {
    Preconditions.checkNotNull(account, "account can not be null.");

    return remoteDataStore.add(account)
        .subscribeOn(Schedulers.io())
        .doOnSuccess(diskDataStore::add);
  }

  /**
   * List by name observable.
   *
   * @param name the name
   * @return the observable
   */
  public Observable<List<AccountRealm>> listByName(String name) {
    return remoteDataStore.list(name).subscribeOn(Schedulers.io());
  }
}