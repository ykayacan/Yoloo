package com.yoloo.android.data.repository.tag.datasource;

import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.model.TagRealmFields;
import com.yoloo.android.data.sorter.TagSorter;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;

public class TagDiskDataStore {

  private static TagDiskDataStore instance;

  private TagDiskDataStore() {
  }

  public static TagDiskDataStore getInstance() {
    if (instance == null) {
      instance = new TagDiskDataStore();
    }
    return instance;
  }

  public void addAll(List<TagRealm> tags) {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> tx.insertOrUpdate(tags));

    realm.close();
  }

  public void replace(List<TagRealm> realms) {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> {
      tx.where(TagRealm.class)
          .equalTo(TagRealmFields.IS_RECOMMENDED, true)
          .findAll()
          .deleteAllFromRealm();

      tx.insert(realms);
    });

    realm.close();
  }

  public Observable<List<TagRealm>> list(TagSorter sorter) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      List<TagRealm> tags;
      switch (sorter) {
        case RECOMMENDED:
          tags = realm.copyFromRealm(realm.where(TagRealm.class)
              .equalTo(TagRealmFields.IS_RECOMMENDED, true)
              .findAllSorted(TagRealmFields.POST_COUNT, Sort.DESCENDING));
          break;
        case DEFAULT:
        default:
          tags = realm.copyFromRealm(realm.where(TagRealm.class).findAll());
          break;
      }

      return tags;
    });
  }

  public Observable<List<TagRealm>> listRecent() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<TagRealm> results =
          realm.where(TagRealm.class).equalTo(TagRealmFields.RECENT, true).findAll();

      if (results.isEmpty()) {
        realm.close();
        return Collections.emptyList();
      } else {
        List<TagRealm> tags = realm.copyFromRealm(results);
        realm.close();
        return tags;
      }
    });
  }
}
