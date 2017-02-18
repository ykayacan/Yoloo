package com.yoloo.android.data.repository.post.datasource;

import com.yoloo.android.data.ApiManager;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.PostFaker;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.sorter.PostSorter;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PostRemoteDataStore {

  private static PostRemoteDataStore instance;

  private PostRemoteDataStore() {
  }

  public static PostRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new PostRemoteDataStore();
    }
    return instance;
  }

  public Observable<PostRealm> get(String postId) {
    return ApiManager.getIdToken().toObservable().flatMap(s -> Observable.empty());
  }

  public Observable<PostRealm> add(PostRealm post) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.just(post.setId(UUID.randomUUID().toString())));
  }

  public Completable delete(String postId) {
    return Completable.complete();
  }

  public Observable<Response<List<PostRealm>>> listByFeed(String cursor, String eTag,
      int limit) {

    return Observable.just(Response.create(PostFaker.generateAll(), null, null))
        .delay(700, TimeUnit.MILLISECONDS);
  }

  public Observable<Response<List<PostRealm>>> listByBounty(String cursor, String eTag, int limit) {
    return ApiManager.getIdToken().toObservable().flatMap(s -> Observable.empty());
  }

  public Observable<Response<List<PostRealm>>> listByCategory(String categoryName,
      PostSorter sorter, String cursor, String eTag, int limit) {
    return ApiManager.getIdToken().toObservable().flatMap(s -> Observable.empty());
  }

  public Observable<Response<List<PostRealm>>> listByTags(String tagNames,
      PostSorter sorter, String cursor, String eTag, int limit) {
    return ApiManager.getIdToken().toObservable().flatMap(s -> Observable.empty());
  }

  public Observable<Response<List<PostRealm>>> listByUser(String userId, boolean commented,
      String cursor, String eTag, int limit) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  public Observable<Response<List<PostRealm>>> listByBookmarked(String cursor, String eTag,
      int limit) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  public Observable<PostRealm> vote(String postId, int direction) {
    return ApiManager.getIdToken().toObservable().flatMap(s -> Observable.empty());
        /*ApiManager.instance.getApi().questions()
            .votePost(postId, String.valueOf(direction))
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()*/
  }

  /*private CollectionResponseFeedItem getFeedApi(String idToken, String cursor, int limit,
      String eTag) throws IOException {
    return ApiManager.instance.getApi()
        .accounts()
        .feed()
        .setCursor(cursor)
        .setLimit(limit)
        .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
        .setRequestHeaders(new HttpHeaders().setIfNoneMatch(eTag))
        .execute();
  }*/
}
