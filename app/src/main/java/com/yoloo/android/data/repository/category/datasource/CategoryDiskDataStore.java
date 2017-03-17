package com.yoloo.android.data.repository.category.datasource;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.CategoryRealmFields;
import com.yoloo.android.data.sorter.CategorySorter;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class CategoryDiskDataStore {

  private static CategoryDiskDataStore instance;

  private CategoryDiskDataStore() {
  }

  public static CategoryDiskDataStore getInstance() {
    if (instance == null) {
      instance = new CategoryDiskDataStore();
    }
    return instance;
  }

  public void addAll(List<CategoryRealm> categories) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(categories));
    realm.close();
  }

  public Observable<List<CategoryRealm>> list(CategorySorter sorter, int limit) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmQuery<CategoryRealm> query = realm.where(CategoryRealm.class);

      if (sorter == CategorySorter.TRENDING) {
        RealmResults<CategoryRealm> results =
            query.findAllSorted(CategoryRealmFields.RANK, Sort.DESCENDING);

        List<CategoryRealm> categories = results.isEmpty()
            ? Collections.emptyList()
            : realm.copyFromRealm(results, limit);

        realm.close();

        return categories;
      } else {
        RealmResults<CategoryRealm> results = query.findAll();

        List<CategoryRealm> categories = results.isEmpty()
            ? Collections.emptyList()
            : realm.copyFromRealm(results, limit);

        realm.close();

        return categories;
      }
    });
  }
}
