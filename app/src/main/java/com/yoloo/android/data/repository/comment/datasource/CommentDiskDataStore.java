package com.yoloo.android.data.repository.comment.datasource;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.CommentRealmFields;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;

public class CommentDiskDataStore {

  private static CommentDiskDataStore INSTANCE;

  private CommentDiskDataStore() {
  }

  public static CommentDiskDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new CommentDiskDataStore();
    }
    return INSTANCE;
  }

  /**
   * Get observable.
   *
   * @param commentId the comment id
   * @return the observable
   */
  public Observable<CommentRealm> get(String commentId) {
    Realm realm = Realm.getDefaultInstance();

    CommentRealm comment = realm.copyFromRealm(
        realm.where(CommentRealm.class).equalTo(CommentRealmFields.ID, commentId).findFirst());

    realm.close();

    return Observable.just(comment);
  }

  /**
   * Add.
   *
   * @param comment the comment
   */
  public void add(CommentRealm comment) {
    addAll(Collections.singletonList(comment));
  }

  /**
   * Add all.
   *
   * @param comments the comments
   */
  public void addAll(List<CommentRealm> comments) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> tx.insertOrUpdate(comments));
    realm.close();
  }

  /**
   * Delete.
   *
   * @param commentId the comment id
   */
  public void delete(String commentId) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> {
      CommentRealm comment =
          tx.where(CommentRealm.class).equalTo(CommentRealmFields.ID, commentId).findFirst();

      if (comment.isValid() && comment.isLoaded()) {
        comment.deleteFromRealm();
      }
    });
    realm.close();
  }

  /**
   * List observable.
   *
   * @param postId the post id
   * @return the observable
   */
  public Observable<Response<List<CommentRealm>>> list(String postId) {
    Realm realm = Realm.getDefaultInstance();

    List<CommentRealm> comments = realm.copyFromRealm(realm.where(CommentRealm.class)
        .equalTo(CommentRealmFields.POST_ID, postId)
        .findAllSorted(CommentRealmFields.CREATED, Sort.DESCENDING));

    realm.close();

    return Observable.just(Response.create(comments, null, null));
  }

  public void vote(String commentId, int direction) {
    Realm realm = Realm.getDefaultInstance();
    CommentRealm comment =
        realm.where(CommentRealm.class).equalTo(CommentRealmFields.ID, commentId).findFirst();

    realm.executeTransaction(tx -> {
      setVoteCounter(direction, comment);
      comment.setDir(direction);
      tx.insertOrUpdate(comment);
    });
    realm.close();
  }

  private void setVoteCounter(int direction, CommentRealm comment) {
    switch (comment.getDir()) {
      case 0:
        if (direction == 1) {
          comment.increaseVotes();
        } else if (direction == -1) {
          comment.decreaseVotes();
        }
        break;
      case 1:
        if (direction == 0) {
          comment.decreaseVotes();
        } else if (direction == -1) {
          comment.decreaseVotes();
          comment.decreaseVotes();
        }
        break;
      case -1:
        if (direction == 0) {
          comment.increaseVotes();
        } else if (direction == 1) {
          comment.increaseVotes();
          comment.increaseVotes();
        }
        break;
    }
  }
}
