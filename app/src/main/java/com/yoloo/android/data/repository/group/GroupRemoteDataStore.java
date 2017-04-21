package com.yoloo.android.data.repository.group;

import com.annimon.stream.Stream;
import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.repository.user.UserResponseTransformer;
import com.yoloo.android.data.sorter.GroupSorter;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import timber.log.Timber;

import static com.yoloo.android.data.ApiManager.INSTANCE;
import static com.yoloo.android.data.ApiManager.getIdToken;

class GroupRemoteDataStore {

  private static GroupRemoteDataStore instance;

  private GroupRemoteDataStore() {
  }

  static GroupRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new GroupRemoteDataStore();
    }
    return instance;
  }

  Single<GroupRealm> get(@Nonnull String groupId) {
    return getIdToken()
        .flatMap(idToken -> Single
            .fromCallable(() -> INSTANCE
                .getApi()
                .groups()
                .get(groupId)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(GroupRealm::new);
  }

  Observable<Response<List<GroupRealm>>> list(@Nonnull GroupSorter sorter, @Nullable String cursor,
      int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .groups()
                .list()
                .setSort(sorter.name())
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(GroupResponseTransformer.create());
  }

  Observable<List<GroupRealm>> listSubscribedGroups(@Nonnull String userId) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .groups()
                .subscribedGroups(userId)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(response -> Stream.of(response.getItems()).map(GroupRealm::new).toList());
  }

  /**
   * List group users observable.
   *
   * @param groupId the group id
   * @return the observable
   */
  Observable<Response<List<AccountRealm>>> listGroupUsers(@Nonnull String groupId,
      @Nullable String cursor, int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .groups()
                .users(groupId)
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(UserResponseTransformer.create());
  }

  /**
   * Search groups observable.
   *
   * @param query the query
   * @return the observable
   */
  Observable<List<GroupRealm>> searchGroups(@Nonnull String query) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .groups()
                .search(query)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .filter(collection -> !collection.getItems().isEmpty())
        .map(collection -> Stream.of(collection.getItems()).map(GroupRealm::new).toList());
  }

  /**
   * Subscribe completable.
   *
   * @param groupId the group id
   * @return the completable
   */
  Completable subscribe(@Nonnull String groupId) {
    return getIdToken().flatMapCompletable(idToken -> Completable.fromCallable(() -> INSTANCE
        .getApi()
        .groups()
        .subscribe(groupId)
        .setRequestHeaders(setIdTokenHeader(idToken))
        .execute()));
  }

  /**
   * Unsubscribe completable.
   *
   * @param groupId the group id
   * @return the completable
   */
  Completable unsubscribe(@Nonnull String groupId) {
    return getIdToken().flatMapCompletable(idToken -> Completable.fromCallable(() -> INSTANCE
        .getApi()
        .groups()
        .unsubscribe(groupId)
        .setRequestHeaders(setIdTokenHeader(idToken))
        .execute()));
  }

  private HttpHeaders setIdTokenHeader(@Nonnull String idToken) {
    Timber.d("Token: %s", idToken);
    return new HttpHeaders().setAuthorization("Bearer " + idToken);
  }
}
