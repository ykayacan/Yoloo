package com.yoloo.android.data.repository.comment.datasource;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.CommentRealmFields;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.PostRealmFields;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.Sort;
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
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      CommentRealm comment = realm.copyFromRealm(realm.where(CommentRealm.class)
          .equalTo(CommentRealmFields.ID, commentId)
          .findFirst());

      realm.close();

      return comment;
    });
  }

  /**
   * Add.
   *
   * @param comment the comment
   */
  public Completable add(CommentRealm comment) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        PostRealm post = tx.where(PostRealm.class)
            .equalTo(PostRealmFields.ID, comment.getPostId())
            .findFirst();
        post.increaseComments();

        if (comment.isAccepted()) {
          post.setAcceptedCommentId(comment.getId());
        }

        tx.insertOrUpdate(post);
        tx.insertOrUpdate(comment);
      });

      realm.close();
    });
  }

  /**
   * Add all.
   *
   * @param comments the comments
   */
  public Completable addAll(List<CommentRealm> comments) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();
      realm.executeTransaction(tx -> tx.insertOrUpdate(comments));
      realm.close();
    });
  }

  /**
   * Delete.
   *
   * @param commentId the comment id
   */
  public Completable delete(String commentId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> tx.where(CommentRealm.class)
          .equalTo(CommentRealmFields.ID, commentId)
          .findFirst().deleteFromRealm());

      realm.close();
    });
  }

  /**
   * List observable.
   *
   * @param postId the post id
   * @return the observable
   */
  public Observable<Response<List<CommentRealm>>> list(String postId) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      List<CommentRealm> comments = realm.copyFromRealm(realm.where(CommentRealm.class)
          .equalTo(CommentRealmFields.POST_ID, postId)
          .findAllSorted(CommentRealmFields.CREATED, Sort.DESCENDING));

      realm.close();

      return Response.create(comments, null, null);
    });
  }

  public Completable vote(String commentId, int direction) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      CommentRealm comment = realm.where(CommentRealm.class)
          .equalTo(CommentRealmFields.ID, commentId)
          .findFirst();

      realm.executeTransaction(tx -> {
        setVoteCounter(direction, comment);
        comment.setDir(direction);
        tx.insertOrUpdate(comment);
      });

      realm.close();
    });
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
