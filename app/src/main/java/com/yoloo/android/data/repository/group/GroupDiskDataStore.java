package com.yoloo.android.data.repository.group;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.db.GroupRealmFields;
import com.yoloo.android.data.sorter.GroupSorter;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

class GroupDiskDataStore {

  private static GroupDiskDataStore instance;

  private GroupDiskDataStore() {
  }

  static GroupDiskDataStore getInstance() {
    if (instance == null) {
      instance = new GroupDiskDataStore();
    }
    return instance;
  }

  void addAll(@Nonnull List<GroupRealm> categories) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(categories));
    realm.close();
  }

  Observable<Response<List<GroupRealm>>> list(@Nonnull GroupSorter sorter, int limit) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmQuery<GroupRealm> query = realm.where(GroupRealm.class);

      if (sorter == GroupSorter.TRENDING) {
        RealmResults<GroupRealm> results =
            query.findAllSorted(GroupRealmFields.RANK, Sort.DESCENDING);

        List<GroupRealm> groups =
            results.isEmpty() ? Collections.emptyList() : realm.copyFromRealm(results, limit);

        realm.close();

        return Response.create(groups, null);
      } else {
        RealmResults<GroupRealm> results = query.findAll();

        List<GroupRealm> groups =
            results.isEmpty() ? Collections.emptyList() : realm.copyFromRealm(results, limit);

        realm.close();

        return Response.create(groups, null);
      }
    });
  }

  Completable subscribe(@Nonnull String groupId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();
      realm.executeTransaction(tx -> {
        GroupRealm group =
            tx.where(GroupRealm.class).equalTo(GroupRealmFields.ID, groupId).findFirst();

        if (group != null) {
          group.setSubscribed(true);
          tx.insertOrUpdate(group);
        }
      });
      realm.close();
    });
  }

  Completable unsubscribe(@Nonnull String groupId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();
      realm.executeTransaction(tx -> {
        GroupRealm group =
            tx.where(GroupRealm.class).equalTo(GroupRealmFields.ID, groupId).findFirst();

        if (group != null) {
          group.setSubscribed(false);
          tx.insertOrUpdate(group);
        }
      });
      realm.close();
    });
  }
}
