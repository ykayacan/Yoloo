package com.yoloo.android.data.repository.category.datasource;

import com.annimon.stream.Stream;
import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.sorter.CategorySorter;

import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Observable;

import static com.yoloo.android.data.ApiManager.INSTANCE;
import static com.yoloo.android.data.ApiManager.getIdToken;

public class CategoryRemoteDataStore {

  private static CategoryRemoteDataStore instance;

  private CategoryRemoteDataStore() {
  }

  public static CategoryRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new CategoryRemoteDataStore();
    }
    return instance;
  }

  public Observable<List<CategoryRealm>> list(@Nonnull CategorySorter sorter, int limit) {
    return Observable.fromCallable(() ->
        INSTANCE.getApi()
            .categories()
            .list()
            .setSort(sorter.name())
            .setLimit(limit)
            .execute())
        .map(response -> Stream.of(response.getItems()).map(CategoryRealm::new).toList());
  }

  public Observable<List<CategoryRealm>> listInterestedCategories(@Nonnull String userId) {
    return getIdToken()
        .flatMapObservable(idToken ->
            Observable.fromCallable(() ->
                INSTANCE.getApi()
                    .categories()
                    .interestedCategories(userId)
                    .setRequestHeaders(setIdTokenHeader(idToken))
                    .execute()))
        .map(response -> Stream.of(response.getItems()).map(CategoryRealm::new).toList());
  }

  private HttpHeaders setIdTokenHeader(@Nonnull String idToken) {
    return new HttpHeaders().setAuthorization("Bearer " + idToken);
  }
}
