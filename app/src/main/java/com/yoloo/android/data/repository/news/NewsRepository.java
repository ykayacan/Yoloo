package com.yoloo.android.data.repository.news;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.data.repository.news.datasource.NewsRemoteDataStore;
import io.reactivex.Observable;
import java.util.List;
import javax.annotation.Nullable;

public class NewsRepository {

  private static NewsRepository instance;

  private final NewsRemoteDataStore remoteDataStore;

  private NewsRepository(NewsRemoteDataStore remoteDataStore) {
    this.remoteDataStore = remoteDataStore;
  }

  public static NewsRepository getInstance(NewsRemoteDataStore remoteDataStore) {
    if (instance == null) {
      instance = new NewsRepository(remoteDataStore);
    }
    return instance;
  }

  public Observable<Response<List<NewsRealm>>> listNews(@Nullable String cursor, int limit) {
    return remoteDataStore.list(cursor, limit);
  }
}
