package com.yoloo.android.data.repository.post.datasource;

import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.backend.modal.yolooApi.model.CollectionResponseFeedItem;
import com.yoloo.android.backend.modal.yolooApi.model.FeedItem;
import com.yoloo.android.data.ApiManager;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.sorter.PostSorter;
import io.reactivex.Observable;
import java.io.IOException;
import java.util.List;

public class PostRemoteDataStore {

  private static PostRemoteDataStore INSTANCE;

  private PostRemoteDataStore() {
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static PostRemoteDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PostRemoteDataStore();
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
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  /**
   * Add observable.
   *
   * @param post the post realm
   * @return the observable
   */
  public Observable<PostRealm> add(PostRealm post) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  /**
   * Delete.
   *
   * @param postId the post id
   */
  public void delete(String postId) {

  }

  /**
   * List observable.
   *
   * @param sorter the sorter
   * @param category the category
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> list(PostSorter sorter,
      String category, String cursor, String eTag, int limit) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }

  /**
   * List feed observable.
   *
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit
   * @return the observable
   */
  public Observable<Response<List<PostRealm>>> listFeed(String cursor, String eTag,
      int limit) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
        /*.fromCallable(() -> getFeedApi(idToken, cursor, limit, eTag))
        .map(response -> {
          List<PostRealm> list = Observable.fromIterable(response.getItems())
              .map(this::mapToFeedModel)
              .toList()
              .blockingGet();

          return Response.create(list, response.getNextPageToken(), (String) response.get("etag"));
        });*/
  }

  public Observable<PostRealm> vote(String postId, int direction) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
        /*ApiManager.INSTANCE.getApi().questions()
            .vote(postId, String.valueOf(direction))
            .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
            .execute()*/
  }

  private CollectionResponseFeedItem getFeedApi(String idToken, String cursor,
      int limit, String eTag) throws IOException {
    return ApiManager.INSTANCE.getApi().accounts().feed()
        .setCursor(cursor)
        .setLimit(limit)
        .setRequestHeaders(new HttpHeaders().setAuthorization("Bearer " + idToken))
        .setRequestHeaders(new HttpHeaders().setIfNoneMatch(eTag))
        .execute();
  }

  private PostRealm mapToFeedModel(FeedItem item) {
    return new PostRealm(); /*PostRealm.builder()
        .id((String) item.get("id"))
        .username((String) item.get("username"))
        .avatarUrl((String) item.get("avatarUrl"))
        .content((String) item.get("content"))
        .ownerId((String) item.get("ownerId"))
        .acceptedCommentId((String) item.get("acceptedCommentId"))
        .build();*/
  }
}