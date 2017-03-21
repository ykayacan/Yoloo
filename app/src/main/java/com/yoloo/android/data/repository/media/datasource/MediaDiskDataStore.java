package com.yoloo.android.data.repository.media.datasource;

import com.annimon.stream.Optional;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.CommentRealmFields;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.PostRealmFields;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MediaDiskDataStore {

  private static MediaDiskDataStore instance;

  private MediaDiskDataStore() {
  }

  public static MediaDiskDataStore getInstance() {
    if (instance == null) {
      instance = new MediaDiskDataStore();
    }
    return instance;
  }

  public Single<Optional<CommentRealm>> get(@Nonnull String commentId) {
    return Single.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      CommentRealm result = realm.where(CommentRealm.class)
          .equalTo(CommentRealmFields.ID, commentId).findFirst();

      Optional<CommentRealm> comment = result == null
          ? Optional.empty()
          : Optional.of(realm.copyFromRealm(result));

      realm.close();

      return comment;
    });
  }

  public void add(@Nonnull CommentRealm comment) {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> {
      PostRealm post = tx.where(PostRealm.class)
          .equalTo(PostRealmFields.ID, comment.getPostId()).findFirst();

      if (post != null) {
        post.increaseCommentCount();

        if (comment.isAccepted()) {
          post.setAcceptedCommentId(comment.getId());
        }

        tx.insertOrUpdate(post);
        tx.insertOrUpdate(comment);
      }
    });

    realm.close();
  }

  public void addAll(@Nonnull List<CommentRealm> comments) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(comments));
    realm.close();
  }

  public Completable delete(@Nonnull CommentRealm comment) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        PostRealm post = tx.where(PostRealm.class)
            .equalTo(PostRealmFields.ID, comment.getPostId())
            .findFirst();

        if (post != null) {
          post.decreaseCommentCount();

          if (comment.isAccepted()) {
            post.setAcceptedCommentId(null);
          }
          tx.insertOrUpdate(post);
        }

        CommentRealm result = tx.where(CommentRealm.class)
            .equalTo(CommentRealmFields.ID, comment.getId())
            .findFirst();

        if (result != null) {
          result.deleteFromRealm();
        }
      });

      realm.close();
    });
  }

  public Observable<Response<List<CommentRealm>>> list(@Nonnull String postId) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<CommentRealm> results = (realm.where(CommentRealm.class)
          .equalTo(CommentRealmFields.POST_ID, postId)
          .findAllSorted(CommentRealmFields.CREATED, Sort.DESCENDING));

      List<CommentRealm> comments = results.isEmpty()
          ? Collections.emptyList()
          : realm.copyFromRealm(results);

      realm.close();

      return Response.create(comments, null);
    });
  }

  public Completable vote(@Nonnull String commentId, int direction) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        CommentRealm comment = tx.where(CommentRealm.class)
            .equalTo(CommentRealmFields.ID, commentId).findFirst();

        if (comment != null) {
          setVoteCounter(direction, comment);
          comment.setVoteDir(direction);
          tx.insertOrUpdate(comment);
        }
      });

      realm.close();
    });
  }

  public void accept(@Nonnull CommentRealm comment) {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> {
      PostRealm post = tx.where(PostRealm.class)
          .equalTo(PostRealmFields.ID, comment.getPostId()).findFirst();

      if (post != null) {
        if (comment.isAccepted()) {
          post.setAcceptedCommentId(comment.getId());
        }

        tx.insertOrUpdate(post);
        tx.insertOrUpdate(comment);
      }
    });

    realm.close();
  }

  private void setVoteCounter(int direction, CommentRealm comment) {
    switch (comment.getVoteDir()) {
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
