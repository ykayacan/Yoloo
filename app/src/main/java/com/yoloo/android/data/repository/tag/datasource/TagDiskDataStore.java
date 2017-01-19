package com.yoloo.android.data.repository.tag.datasource;

import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.model.TagRealmFields;
import com.yoloo.android.data.sorter.TagSorter;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.List;

public class TagDiskDataStore {

  private static TagDiskDataStore INSTANCE;

  private TagDiskDataStore() {
  }

  public static TagDiskDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TagDiskDataStore();
    }
    return INSTANCE;
  }

  public void addAll(List<TagRealm> tags) {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> tx.insertOrUpdate(tags));

    realm.close();
  }

  public void replace(List<TagRealm> realms) {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      tx.where(TagRealm.class)
          .equalTo(TagRealmFields.IS_RECOMMENDED, true)
          .findAll()
          .deleteAllFromRealm();

      tx.insert(realms);
    });

    realm.close();
  }

  public Observable<List<TagRealm>> list(TagSorter sorter) {
    Realm realm = Realm.getDefaultInstance();

    List<TagRealm> tagRealms;
    switch (sorter) {
      case RECOMMENDED:
        tagRealms = realm.copyFromRealm(
            realm.where(TagRealm.class)
                .equalTo(TagRealmFields.IS_RECOMMENDED, true)
                .findAllSorted(TagRealmFields.POSTS, Sort.DESCENDING));
        break;
      case DEFAULT:
      default:
        tagRealms = realm.copyFromRealm(
            realm.where(TagRealm.class)
                .findAll());
        break;
    }

    realm.close();

    return Observable.just(tagRealms);
  }

  public Observable<List<TagRealm>> listRecent() {
    return Observable.create(e -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<TagRealm> results = realm.where(TagRealm.class)
          .equalTo(TagRealmFields.RECENT, true)
          .findAllAsync();

      final RealmChangeListener<RealmResults<TagRealm>> listener = element -> {
        e.onNext(realm.copyFromRealm(results));
        e.onComplete();

        realm.close();
      };

      results.addChangeListener(listener);

      e.setCancellable(() -> results.removeChangeListener(listener));
    });
  }
}
