package com.yoloo.android.data.repository.comment;

import com.annimon.stream.Optional;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.AccountRealmFields;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.datasource.CommentDiskDataStore;
import com.yoloo.android.data.repository.comment.datasource.CommentRemoteDataStore;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommentRepository {

  private static CommentRepository instance;

  private final CommentRemoteDataStore remoteDataStore;
  private final CommentDiskDataStore diskDataStore;

  private CommentRepository(CommentRemoteDataStore remoteDataStore,
      CommentDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static CommentRepository getInstance(CommentRemoteDataStore remoteDataStore,
      CommentDiskDataStore diskDataStore) {
    if (instance == null) {
      instance = new CommentRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  public Observable<Optional<CommentRealm>> getComment(@Nonnull String postId,
      @Nonnull String commentId) {
    return Observable.mergeDelayError(
        diskDataStore.get(commentId).subscribeOn(Schedulers.io()).toObservable(),
        remoteDataStore.get(postId, commentId)
            .doOnSuccess(diskDataStore::add)
            .map(Optional::of)
            .subscribeOn(Schedulers.io())
            .toObservable());
  }

  public Observable<CommentRealm> addComment(@Nonnull CommentRealm comment) {
    Realm realm = Realm.getDefaultInstance();
    AccountRealm account = realm.copyFromRealm(
        realm.where(AccountRealm.class).equalTo(AccountRealmFields.ME, true).findFirst());
    realm.close();

    comment.setOwnerId(account.getId())
        .setUsername(account.getUsername())
        .setAvatarUrl(account.getAvatarUrl());

    return remoteDataStore.add(comment).doOnNext(diskDataStore::add).subscribeOn(Schedulers.io());
  }

  public Completable deleteComment(@Nonnull CommentRealm comment) {
    return remoteDataStore.delete(comment)
        .andThen(diskDataStore.delete(comment))
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<CommentRealm>>> listComments(@Nonnull String postId,
      @Nullable String cursor, int limit) {
    // TODO: 18.02.2017 Change structure
    return diskDataStore.list(postId).subscribeOn(Schedulers.io());

    /*return Observable.mergeDelayError(
        diskDataStore.listNotifications(postId).subscribeOn(Schedulers.io()),
        remoteDataStore.listNotifications(postId, cursor, eTag, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()))
            .subscribeOn(Schedulers.io()));*/
  }

  public Completable voteComment(@Nonnull String commentId, int direction) {
    return diskDataStore.vote(commentId, direction).subscribeOn(Schedulers.io());
  }

  public Observable<CommentRealm> acceptComment(@Nonnull CommentRealm comment) {
    return remoteDataStore.accept(comment)
        .doOnNext(diskDataStore::accept)
        .subscribeOn(Schedulers.io());
  }
}
