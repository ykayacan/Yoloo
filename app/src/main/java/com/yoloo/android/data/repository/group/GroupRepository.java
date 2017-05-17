package com.yoloo.android.data.repository.group;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.data.sorter.GroupSorter;
import io.reactivex.Completable;
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
   * @param sorter the sorter
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<GroupRealm>>> listGroups(@Nonnull GroupSorter sorter,
      @Nullable String cursor, int limit) {
    Observable<Response<List<GroupRealm>>> diskObservable =
        diskDataStore.list(sorter, limit).subscribeOn(Schedulers.io());

    Observable<Response<List<GroupRealm>>> remoteObservable = remoteDataStore
        .list(sorter, cursor, limit)
        .doOnNext(response -> diskDataStore.addAll(response.getData()));

    return diskObservable.flatMap(response -> {
      if (response.getData().isEmpty() || response.getData().size() < 10) {
        return remoteObservable;
      }

      return Observable.just(response);
    });
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
  public Completable subscribe(@Nonnull String groupId) {
    return remoteDataStore.subscribe(groupId)
        .andThen(diskDataStore.subscribe(groupId))
        .subscribeOn(Schedulers.io());
  }

  /**
   * Unsubscribe completable.
   *
   * @param groupId the group id
   * @return the completable
   */
  public Completable unsubscribe(@Nonnull String groupId) {
    return remoteDataStore.unsubscribe(groupId)
        .andThen(diskDataStore.unsubscribe(groupId))
        .subscribeOn(Schedulers.io());
  }
}
