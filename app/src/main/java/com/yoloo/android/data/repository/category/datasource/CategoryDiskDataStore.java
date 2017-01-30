package com.yoloo.android.data.repository.category.datasource;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.CategoryRealmFields;
import com.yoloo.android.data.sorter.CategorySorter;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryDiskDataStore {

  private static CategoryDiskDataStore INSTANCE;

  private CategoryDiskDataStore() {
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static CategoryDiskDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new CategoryDiskDataStore();
    }
    return INSTANCE;
  }

  /**
   * Add.
   *
   * @param categoryRealm the category realm
   */
  public void add(CategoryRealm categoryRealm) {
    addAll(Collections.singletonList(categoryRealm));
  }

  /**
   * Add all.
   *
   * @param realms the realms
   */
  public void addAll(List<CategoryRealm> realms) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(realms));
    realm.close();
  }

  /**
   * List observable.
   *
   * @param sorter the sorter
   * @return the observable
   */
  public Observable<List<CategoryRealm>> list(CategorySorter sorter) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      List<CategoryRealm> categories;
      switch (sorter) {
        case TRENDING:
          RealmResults<CategoryRealm> results = realm.where(CategoryRealm.class)
              .findAllSorted(CategoryRealmFields.RANK, Sort.DESCENDING);

          categories = new ArrayList<>(7);
          for (int i = 0; i < 7; i++) {
            categories.add(realm.copyFromRealm(results.get(i)));
          }
          break;
        case DEFAULT:
        default:
          categories = realm.copyFromRealm(realm.where(CategoryRealm.class).findAll());
          break;
      }

      realm.close();

      return categories;
    });
  }
}