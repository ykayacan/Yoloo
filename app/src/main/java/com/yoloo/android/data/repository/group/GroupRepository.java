package com.yoloo.android.data.repository.group;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.sorter.GroupSorter;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GroupRepository {

  private static GroupRepository instance;

  private final GroupRemoteDataStore remoteDataStore;
  private final GroupDiskDataStore diskDataStore;

  private GroupRepository(GroupRemoteDataStore remoteDataStore, GroupDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static GroupRepository getInstance(GroupRemoteDataStore remoteDataStore,
      GroupDiskDataStore diskDataStore) {
    if (instance == null) {
      instance = new GroupRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  public Single<GroupRealm> getGroup(@Nonnull String groupId) {
    return remoteDataStore.get(groupId);
  }

  public Observable<Response<List<GroupRealm>>> listGroups(@Nonnull GroupSorter sorter,
      @Nullable String cursor, int limit) {
    Observable<Response<List<GroupRealm>>> diskObservable =
        diskDataStore.list(sorter, limit).subscribeOn(Schedulers.io());

    Observable<Response<List<GroupRealm>>> remoteObservable = remoteDataStore
        .list(sorter, cursor, limit)
        .doOnNext(response -> diskDataStore.addAll(response.getData()));

    return Observable.mergeDelayError(diskObservable, remoteObservable).distinct();
  }

  public Observable<List<GroupRealm>> listSubscribedGroups(@Nonnull String userId) {
    return remoteDataStore.listSubscribedGroups(userId);
  }

  public Observable<Response<List<AccountRealm>>> listGroupUsers(@Nonnull String groupId,
      @Nullable String cursor, int limit) {
    return remoteDataStore.listGroupUsers(groupId, cursor, limit).subscribeOn(Schedulers.io());
  }

  public Observable<List<GroupRealm>> searchGroups(@Nonnull String query) {
    return remoteDataStore.searchGroups(query);
  }

  public Completable subscribe(@Nonnull String groupId) {
    return remoteDataStore.subscribe(groupId).subscribeOn(Schedulers.io());
  }

  public Completable unsubscribe(@Nonnull String groupId) {
    return remoteDataStore.unsubscribe(groupId).subscribeOn(Schedulers.io());
  }
}
