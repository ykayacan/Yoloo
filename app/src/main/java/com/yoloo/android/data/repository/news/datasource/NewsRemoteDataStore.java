package com.yoloo.android.data.repository.news.datasource;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.NewsRealm;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewsRemoteDataStore {

  private static NewsRemoteDataStore instance;

  private NewsRemoteDataStore() {
  }

  public static NewsRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new NewsRemoteDataStore();
    }
    return instance;
  }

  public Observable<Response<List<NewsRealm>>> list(String cursor, int limit) {
    NewsRealm n1 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Interrailde Büyük Gelişme")
        .setCover(true)
        .setBgImageUrl("https://www.uzakrota.com/wp-content/uploads/2017/02/"
            + "edinburgh-730x548.jpg");

    NewsRealm n2 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Vizeler Kalktı")
        .setBgImageUrl(
            "https://www.uzakrota.com/wp-content/uploads/2017/02/mercator-"
                + "Accelya-warburg-pincus-airline-services-730x548.jpg");

    NewsRealm n3 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Gezginler Evi Burada Açılıyor")
        .setBgImageUrl(
            "https://www.uzakrota.com/wp-content/uploads/2017/02/Airbnb-helps-combat-"
                + "winter-blues-with-magical-green-wonderland-in-London-730x492.jpg");

    NewsRealm n4 = new NewsRealm()
        .setId(UUID.randomUUID().toString())
        .setTitle("Yeni Yerler Keşfetmenin Tam Zamanı")
        .setBgImageUrl(
            "http://webneel.com/daily/sites/default/files/images/daily/10-2013/1-"
                + "travel-photography.preview.jpg");

    List<NewsRealm> list = new ArrayList<>();
    list.add(n1);
    list.add(n2);
    list.add(n3);
    list.add(n4);
    return Observable.just(Response.create(list, null));
  }
}
