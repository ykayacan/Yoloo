package com.yoloo.android.data.repository.tag;

import com.annimon.stream.Stream;
import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.transformer.TagResponseTransformer;
import com.yoloo.android.data.sorter.TagSorter;
import com.yoloo.backend.yolooApi.model.TagCollection;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import timber.log.Timber;

import static com.yoloo.android.data.ApiManager.INSTANCE;
import static com.yoloo.android.data.ApiManager.getIdToken;

class TagRemoteDataStore {

  private static TagRemoteDataStore instance;

  private TagRemoteDataStore() {
  }

  static TagRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new TagRemoteDataStore();
    }
    return instance;
  }

  Observable<List<TagRealm>> list(TagSorter sorter) {
    return getIdToken().toObservable().flatMap(s -> Observable.empty());
  }

  Observable<List<TagRealm>> listRecommendedTags() {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .tags()
                .recommended()
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .map(TagCollection::getItems)
        .map(tagDTOS -> Stream.of(tagDTOS).map(TagRealm::new).toList());
  }

  /**
   * List observable.
   *
   * @param query the query
   * @param cursor the cursor
   * @param limit the limit
   * @return the observable
   */
  Observable<Response<List<TagRealm>>> searchTag(@Nonnull String query, @Nullable String cursor,
      int limit) {
    return getIdToken()
        .flatMapObservable(idToken -> Observable
            .fromCallable(() -> INSTANCE
                .getApi()
                .tags()
                .list(query)
                .setCursor(cursor)
                .setLimit(limit)
                .setRequestHeaders(setIdTokenHeader(idToken))
                .execute())
            .subscribeOn(Schedulers.io()))
        .compose(TagResponseTransformer.create());
  }

  private HttpHeaders setIdTokenHeader(@Nonnull String idToken) {
    Timber.d("Id Token: %s", idToken);
    return new HttpHeaders().setAuthorization("Bearer " + idToken);
  }
}
