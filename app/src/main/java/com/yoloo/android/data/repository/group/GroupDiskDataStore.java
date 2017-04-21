package com.yoloo.android.data.repository.group;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.GroupRealmFields;
import com.yoloo.android.data.sorter.GroupSorter;
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

  Observable<List<GroupRealm>> list(@Nonnull List<String> categoryIds) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmQuery<GroupRealm> query = realm.where(GroupRealm.class);

      Stream.of(categoryIds).forEach(id -> query.equalTo(GroupRealmFields.ID, id));

      RealmResults<GroupRealm> results = query.findAll();

      List<GroupRealm> categories =
          results.isEmpty() ? Collections.emptyList() : realm.copyFromRealm(results);

      realm.close();

      return categories;
    });
  }
}
