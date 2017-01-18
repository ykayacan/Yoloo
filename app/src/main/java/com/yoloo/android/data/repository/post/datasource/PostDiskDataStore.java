package com.yoloo.android.data.repository.post.datasource;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.PostRealmFields;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;

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
    addAll(Collections.singletonList(post));
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

  /**
   * Delete.
   *
   * @param postId the post id
   */
  public void delete(String postId) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> {
      PostRealm post = tx.where(PostRealm.class).equalTo(PostRealmFields.ID, postId).findFirst();

      if (post.isValid() && post.isLoaded()) {
        post.deleteFromRealm();
      }
    });
    realm.close();
  }

  /**
   * List observable.
   *
   * @param category the category
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> list(String category) {
    Realm realm = Realm.getDefaultInstance();
    RealmQuery<PostRealm> query = realm.where(PostRealm.class);
    if (category != null) {
      query.equalTo(PostRealmFields.CATEGORIES.NAME, category);
    } else {
      query.equalTo(PostRealmFields.IS_FEED_ITEM, true);
    }

    List<PostRealm> posts =
        realm.copyFromRealm(query.findAllSorted(PostRealmFields.CREATED, Sort.DESCENDING));
    realm.close();

    return Observable.just(Response.create(posts, null, null));
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