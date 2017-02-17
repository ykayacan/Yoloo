package com.yoloo.android.data.repository.comment.datasource;

import com.yoloo.android.data.ApiManager;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.FakerUtil;
import com.yoloo.android.data.model.CommentRealm;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.Date;
import java.util.List;

public class CommentRemoteDataStore {

  private static CommentRemoteDataStore INSTANCE;

  private CommentRemoteDataStore() {
  }

  public static CommentRemoteDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new CommentRemoteDataStore();
    }
    return INSTANCE;
  }

  public Observable<CommentRealm> get(String commentId) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  public Observable<CommentRealm> add(CommentRealm comment) {
    return Observable.just(comment);
  }

  public Completable delete(CommentRealm comment) {
    ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());

    return Completable.complete();
  }

  public Observable<Response<List<CommentRealm>>> list(String postId, String cursor, String eTag,
      int limit) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  public Observable<CommentRealm> accept(CommentRealm comment) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.just(
            new CommentRealm()
                .setId(comment.getId())
                .setOwnerId("a1")
                .setUsername("krialix")
                .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
                .setAccepted(true)
                .setPostId(comment.getPostId())
                .setContent("Accepted")
                .setCreated(new Date())));
  }
}
