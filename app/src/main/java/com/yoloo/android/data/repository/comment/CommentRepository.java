package com.yoloo.android.data.repository.comment;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.AccountRealmFields;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.datasource.CommentDiskDataStore;
import com.yoloo.android.data.repository.comment.datasource.CommentRemoteDataStore;
import com.yoloo.android.util.Preconditions;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import java.util.List;

public class CommentRepository {

  private static CommentRepository INSTANCE;

  private final CommentRemoteDataStore remoteDataStore;
  private final CommentDiskDataStore diskDataStore;

  private CommentRepository(CommentRemoteDataStore remoteDataStore,
      CommentDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static CommentRepository getInstance(CommentRemoteDataStore remoteDataStore,
      CommentDiskDataStore diskDataStore) {
    if (INSTANCE == null) {
      INSTANCE = new CommentRepository(remoteDataStore, diskDataStore);
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
    Preconditions.checkNotNull(commentId, "commentId can not be null.");

    return Observable.mergeDelayError(
        diskDataStore.get(commentId).subscribeOn(Schedulers.io()),
        remoteDataStore.get(commentId)
            .doOnNext(commentRealm -> diskDataStore.add(commentRealm).subscribe())
            .subscribeOn(Schedulers.io()));
  }

  /**
   * Add observable.
   *
   * @param comment the comment
   * @return the observable
   */
  public Observable<CommentRealm> add(CommentRealm comment) {
    Preconditions.checkNotNull(comment, "comment can not be null.");

    Realm realm = Realm.getDefaultInstance();
    AccountRealm account = realm.copyFromRealm(
        realm.where(AccountRealm.class).equalTo(AccountRealmFields.ME, true).findFirst());
    realm.close();

    comment.setOwnerId(account.getId())
        .setUsername(account.getUsername())
        .setAvatarUrl(account.getAvatarUrl());

    return remoteDataStore.add(comment)
        .doOnNext(commentRealm -> diskDataStore.add(commentRealm).subscribe())
        .subscribeOn(Schedulers.io());
  }

  /**
   * Delete completable.
   *
   * @param commentId the comment id
   * @return the completable
   */
  public Completable delete(String commentId) {
    Preconditions.checkNotNull(commentId, "commentId can not be null.");

    return remoteDataStore.delete(commentId)
        .andThen(diskDataStore.delete(commentId))
        .subscribeOn(Schedulers.io());
  }

  /**
   * List observable.
   *
   * @param postId the post id
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit    @return the observable
   */
  public Observable<Response<List<CommentRealm>>> list(String postId, String cursor, String eTag,
      int limit) {
    Preconditions.checkNotNull(postId, "postId can not be null.");

    return Observable.mergeDelayError(
        diskDataStore.list(postId).subscribeOn(Schedulers.io()),
        remoteDataStore.list(postId, cursor, eTag, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()).subscribe())
            .subscribeOn(Schedulers.io()));
  }

  /**
   * Vote completable.
   *
   * @param commentId the comment id
   * @param direction the direction
   * @return the completable
   */
  public Completable vote(String commentId, int direction) {
    return diskDataStore.vote(commentId, direction).subscribeOn(Schedulers.io());
  }

  public Completable accept(String questionId, String commentId) {
    return remoteDataStore.accept(questionId, commentId)
        .flatMapCompletable(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }
}