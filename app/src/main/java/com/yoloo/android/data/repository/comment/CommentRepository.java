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

  public Observable<CommentRealm> get(String commentId) {
    Preconditions.checkNotNull(commentId, "commentId can not be null.");

    return Observable.mergeDelayError(
        diskDataStore.get(commentId).subscribeOn(Schedulers.io()),
        remoteDataStore.get(commentId)
            .doOnNext(diskDataStore::add)
            .subscribeOn(Schedulers.io()));
  }

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
        .doOnNext(diskDataStore::add)
        .subscribeOn(Schedulers.io());
  }

  public Completable delete(CommentRealm comment) {
    Preconditions.checkNotNull(comment, "comment can not be null.");

    return remoteDataStore.delete(comment)
        .andThen(diskDataStore.delete(comment))
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<CommentRealm>>> list(String postId, String cursor, String eTag,
      int limit) {
    Preconditions.checkNotNull(postId, "postId can not be null.");

    return Observable.mergeDelayError(
        diskDataStore.list(postId).subscribeOn(Schedulers.io()),
        remoteDataStore.list(postId, cursor, eTag, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()))
            .subscribeOn(Schedulers.io()));
  }

  public Completable vote(String commentId, int direction) {
    return diskDataStore.vote(commentId, direction).subscribeOn(Schedulers.io());
  }

  public Observable<CommentRealm> accept(CommentRealm comment) {
    return remoteDataStore.accept(comment)
        .doOnNext(diskDataStore::accept)
        .subscribeOn(Schedulers.io());
  }
}