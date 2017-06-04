package com.yoloo.android.data.repository.group;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.data.sorter.GroupSorter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The type Group repository.
 */
public class GroupRepository {

  private static GroupRepository instance;

  private final GroupRemoteDataStore remoteDataStore;
  private final GroupDiskDataStore diskDataStore;

  private GroupRepository(GroupRemoteDataStore remoteDataStore, GroupDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  /**
   * Gets instance.
   *
   * @param remoteDataStore the remote data store
   * @param diskDataStore the disk data store
   * @return the instance
   */
  public static GroupRepository getInstance(GroupRemoteDataStore remoteDataStore,
      GroupDiskDataStore diskDataStore) {
    if (instance == null) {
      instance = new GroupRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  /**
   * Gets group.
   *
   * @param groupId the group id
   * @return the group
   */
  public Single<GroupRealm> getGroup(@Nonnull String groupId) {
    return remoteDataStore.get(groupId);
  }

  /**
   * List groups observable.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<GroupRealm>>> listGroups(@Nullable String cursor, int limit) {
    Observable<Response<List<GroupRealm>>> diskObservable =
        diskDataStore.list(GroupSorter.DEFAULT, limit).subscribeOn(Schedulers.io());

    Observable<Response<List<GroupRealm>>> remoteObservable = remoteDataStore
        .list(GroupSorter.DEFAULT, cursor, limit)
        .doOnNext(response -> diskDataStore.addAll(response.getData()));

    return diskObservable.flatMap(response ->
        response.getData().size() < 10 ? remoteObservable : Observable.just(response));
  }

  public Observable<Response<List<GroupRealm>>> listRecommendedGroups() {
    return diskDataStore.list(GroupSorter.DEFAULT, 7).subscribeOn(Schedulers.io());
  }

  /**
   * List subscribed groups observable.
   *
   * @param userId the user id
   * @return the observable
   */
  public Observable<List<GroupRealm>> listSubscribedGroups(@Nonnull String userId) {
    return remoteDataStore.listSubscribedGroups(userId);
  }

  /**
   * List group users observable.
   *
   * @param groupId the group id
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<AccountRealm>>> listGroupUsers(@Nonnull String groupId,
      @Nullable String cursor, int limit) {
    return remoteDataStore.listGroupUsers(groupId, cursor, limit).subscribeOn(Schedulers.io());
  }

  /**
   * List group tags observable.
   *
   * @param groupId the group id
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<TagRealm>>> listGroupTags(@Nonnull String groupId,
      @Nullable String cursor, int limit) {
    return remoteDataStore
        .listGroupTags(groupId, cursor, limit)
        .subscribeOn(Schedulers.io());
  }

  /**
   * Search groups observable.
   *
   * @param query the query
   * @return the observable
   */
  public Observable<List<GroupRealm>> searchGroups(@Nonnull String query) {
    return remoteDataStore.searchGroups(query);
  }

  /**
   * Subscribe completable.
   *
   * @param groupId the group id
   * @return the completable
   */
  public Single<GroupRealm> subscribe(@Nonnull String groupId) {
    return remoteDataStore.subscribe(groupId)
        .flatMap(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  /**
   * Unsubscribe completable.
   *
   * @param groupId the group id
   * @return the completable
   */
  public Single<GroupRealm> unsubscribe(@Nonnull String groupId) {
    return remoteDataStore.unsubscribe(groupId)
        .flatMap(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }
}
