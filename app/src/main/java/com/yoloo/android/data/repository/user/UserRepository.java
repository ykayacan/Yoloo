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
  public Observable<AccountRealm> get(String userId) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    // TODO: 24.01.2017 Convert it to remote.
    return diskDataStore.get(userId).subscribeOn(Schedulers.io());
    /*return remoteDataStore.get(userId)
        .subscribeOn(Schedulers.io());*/
  }

  /**
   * Gets me.
   *
   * @return the me
   */
  public Observable<AccountRealm> getMe() {
    return Observable.mergeDelayError(
        diskDataStore.getMe().subscribeOn(Schedulers.io()),
        remoteDataStore.getMe()
            .doOnNext(diskDataStore::add)
            .subscribeOn(Schedulers.io()));
  }

  /**
   * Gets local me.
   *
   * @return the local me
   */
  public Observable<AccountRealm> getLocalMe() {
    return diskDataStore.getMe().subscribeOn(Schedulers.io()).cache();
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
        .doOnSuccess(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  /**
   * List by name observable.
   *
   * @param query the name
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<AccountRealm>>> search(String query, String cursor, int limit) {
    Preconditions.checkNotNull(query, "query can not be null.");

    return remoteDataStore.list(query, cursor, limit).subscribeOn(Schedulers.io());
  }

  /**
   * List recent searches observable.
   *
   * @return the observable
   */
  public Observable<List<AccountRealm>> listRecentSearches() {
    return diskDataStore.listRecentSearches().subscribeOn(Schedulers.io());
  }

  /**
   * List followers observable.
   *
   * @param userId the user id
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<AccountRealm>>> listFollowers(String userId, String cursor,
      String eTag, int limit) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    return remoteDataStore.listFollowers(userId, cursor, eTag, limit).subscribeOn(Schedulers.io());
  }

  /**
   * List followings observable.
   *
   * @param userId the user id
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<AccountRealm>>> listFollowings(String userId, String cursor,
      String eTag, int limit) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    return remoteDataStore.listFollowings(userId, cursor, eTag, limit).subscribeOn(Schedulers.io());
  }

  /**
   * Follow completable.
   *
   * @param userId the user id
   * @param direction the direction
   * @return the completable
   */
  public Completable follow(String userId, int direction) {
    Preconditions.checkNotNull(userId, "userId can not be null.");

    return remoteDataStore.follow(userId, direction);
  }
}