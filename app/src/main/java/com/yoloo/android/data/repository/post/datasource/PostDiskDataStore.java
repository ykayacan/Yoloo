package com.yoloo.android.data.repository.post.datasource;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.AccountRealmFields;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.CommentRealmFields;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.PostRealmFields;
import com.yoloo.android.data.sorter.PostSorter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.List;
import java.util.UUID;

public class PostDiskDataStore {

  private static PostDiskDataStore INSTANCE;

  private PostDiskDataStore() {
  }

  public static PostDiskDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PostDiskDataStore();
    }
    return INSTANCE;
  }

  /**
   * Get observable.
   *
   * @param postId the post id
   * @return the observable
   */
  public Observable<PostRealm> get(String postId) {
    Realm realm = Realm.getDefaultInstance();

    PostRealm post = realm.copyFromRealm(
        realm.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst());

    realm.close();

    return Observable.just(post);
  }

  /**
   * Add.
   *
   * @param post the post realm
   */
  public void add(PostRealm post) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(post));
    realm.close();
  }

  /**
   * Add all.
   *
   * @param posts the realms
   */
  public void addAll(List<PostRealm> posts) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> tx.insertOrUpdate(posts));
    realm.close();
  }

  public Single<PostRealm> addOrGetDraft() {
    Realm realm = Realm.getDefaultInstance();
    PostRealm oldDraft =
        realm.where(PostRealm.class).equalTo(PostRealmFields.DRAFT, true).findFirst();

    if (oldDraft == null) {
      AccountRealm me = realm.copyFromRealm(
          realm.where(AccountRealm.class).equalTo(AccountRealmFields.ME, true).findFirst());

      PostRealm newDraft = new PostRealm().setId(UUID.randomUUID().toString())
          .setUsername(me.getUsername())
          .setOwnerId(me.getId())
          .setAvatarUrl(me.getAvatarUrl())
          .setDir(0)
          .setFeedItem(true)
          .setDraft(true);

      realm.executeTransactionAsync(tx -> tx.insertOrUpdate(newDraft));
      realm.close();

      return Single.just(newDraft);
    } else {
      PostRealm post = realm.copyFromRealm(oldDraft);
      realm.close();

      return Single.just(post);
    }
  }

  public void updateDraft(PostRealm draft) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(draft));
    realm.close();
  }

  /**
   * Delete.
   *
   * @param postId the post id
   */
  public void delete(String postId) {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      PostRealm post = tx.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst();
      post.deleteFromRealm();

      RealmResults<CommentRealm> commentResults =
          tx.where(CommentRealm.class).equalTo(CommentRealmFields.POST_ID, postId).findAll();
      commentResults.deleteAllFromRealm();
    });

    realm.close();
  }

  public void deleteDraft() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      PostRealm post = tx.where(PostRealm.class).equalTo(PostRealmFields.DRAFT, true).findFirst();
      post.deleteFromRealm();
    });

    realm.close();
  }

  /**
   * List feed observable.
   *
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listFeed() {
    return Observable.create(e -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<PostRealm> results = realm.where(PostRealm.class)
          .equalTo(PostRealmFields.IS_FEED_ITEM, true)
          .notEqualTo(PostRealmFields.PENDING, true)
          .notEqualTo(PostRealmFields.DRAFT, true)
          .findAllSortedAsync(PostRealmFields.CREATED, Sort.DESCENDING);

      final RealmChangeListener<RealmResults<PostRealm>> listener = element -> {
        e.onNext(Response.create(realm.copyFromRealm(element), null, null));
        e.onComplete();

        realm.close();
      };

      results.addChangeListener(listener);

      e.setCancellable(() -> results.removeChangeListener(listener));
    });
  }

  /**
   * List observable.
   *
   * @param sorter the sorter
   * @param category the category
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> list(PostSorter sorter, String category) {
    return Observable.create(e -> {
      Realm realm = Realm.getDefaultInstance();

      RealmQuery<PostRealm> query = realm.where(PostRealm.class);

      RealmResults<PostRealm> results;

      query.notEqualTo(PostRealmFields.PENDING, true).notEqualTo(PostRealmFields.DRAFT, true);

      if (sorter.equals(PostSorter.NEWEST)) {
        results = query.equalTo(PostRealmFields.CATEGORIES.NAME, category)
            .findAllSortedAsync(PostRealmFields.CREATED, Sort.DESCENDING);
      } else if (sorter.equals(PostSorter.HOT)) {
        results = query.equalTo(PostRealmFields.CATEGORIES.NAME, category)
            .findAllSortedAsync(PostRealmFields.RANK, Sort.DESCENDING);
      } else if (sorter.equals(PostSorter.UNANSWERED)) {
        results = query.equalTo(PostRealmFields.CATEGORIES.NAME, category)
            .findAllSortedAsync(PostRealmFields.CREATED, Sort.DESCENDING);
      } else if (sorter.equals(PostSorter.BOUNTY)) {
        results = query.notEqualTo(PostRealmFields.BOUNTY, 0)
            .findAllSortedAsync(PostRealmFields.RANK, Sort.DESCENDING);
      } else {
        results = query.findAllAsync();
      }

      final RealmChangeListener<RealmResults<PostRealm>> listener = element -> {
        e.onNext(Response.create(realm.copyFromRealm(element), null, null));
        e.onComplete();

        realm.close();
      };

      results.addChangeListener(listener);

      e.setCancellable(() -> results.removeChangeListener(listener));
    });
  }

  /**
   * Vote.
   *
   * @param postId the post id
   * @param direction the direction
   */
  public void vote(String postId, int direction) {
    Realm realm = Realm.getDefaultInstance();
    PostRealm post = realm.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst();

    realm.executeTransaction(tx -> {
      setVoteCounter(direction, post);
      post.setDir(direction);
      tx.insertOrUpdate(post);
    });
    realm.close();
  }

  private void setVoteCounter(int direction, PostRealm post) {
    switch (post.getDir()) {
      case 0:
        if (direction == 1) {
          post.increaseVotes();
        } else if (direction == -1) {
          post.decreaseVotes();
        }
        break;
      case 1:
        if (direction == 0) {
          post.decreaseVotes();
        } else if (direction == -1) {
          post.decreaseVotes();
          post.decreaseVotes();
        }
        break;
      case -1:
        if (direction == 0) {
          post.increaseVotes();
        } else if (direction == 1) {
          post.increaseVotes();
          post.increaseVotes();
        }
        break;
    }
  }
}