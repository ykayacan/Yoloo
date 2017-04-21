package com.yoloo.android.data.repository.travelertype;

import com.annimon.stream.Stream;
import com.yoloo.android.data.model.TravelerType;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

import static com.yoloo.android.data.ApiManager.INSTANCE;

class TravelerTypeRemoteDataStore {

  private static TravelerTypeRemoteDataStore instance;

  private TravelerTypeRemoteDataStore() {
  }

  static TravelerTypeRemoteDataStore getInstance() {
    if (instance == null) {
      instance = new TravelerTypeRemoteDataStore();
    }
    return instance;
  }

  Observable<List<TravelerType>> listTravelerTypes() {
    return Observable
        .fromCallable(() -> INSTANCE.getApi().travelerTypes().list().execute())
        .map(response -> Stream.of(response.getItems()).map(TravelerType::new).toList())
        .subscribeOn(Schedulers.io());
  }

  Observable<List<TravelerType>> listTravelerTypes2() {
    com.yoloo.backend.yolooApi.model.TravelerType t1 = new com.yoloo.backend.yolooApi.model.TravelerType()
        .setImageUrl("https://storage.googleapis.com/yoloo-151719.appspot.com/traveler-types/escapist.webp")
        .setName("Escapist");

    com.yoloo.backend.yolooApi.model.TravelerType t2 = new com.yoloo.backend.yolooApi.model.TravelerType()
        .setImageUrl("https://storage.googleapis.com/yoloo-151719.appspot.com/traveler-types/guidebook-memorizer.webp")
        .setName("Guidebook Memorizer");

    com.yoloo.backend.yolooApi.model.TravelerType t3 = new com.yoloo.backend.yolooApi.model.TravelerType()
        .setImageUrl("https://storage.googleapis.com/yoloo-151719.appspot.com/traveler-types/know-it-all.webp")
        .setName("Know It All");

    com.yoloo.backend.yolooApi.model.TravelerType t4 = new com.yoloo.backend.yolooApi.model.TravelerType()
        .setImageUrl("https://storage.googleapis.com/yoloo-151719.appspot.com/traveler-types/no-expense.webp")
        .setName("Know It All");

    TravelerType ty1 = new TravelerType(t1);
    TravelerType ty2 = new TravelerType(t2);
    TravelerType ty3 = new TravelerType(t3);
    TravelerType ty4 = new TravelerType(t4);

    List<TravelerType> types = new ArrayList<>();
    types.add(ty1);
    types.add(ty2);
    types.add(ty3);
    types.add(ty4);

    return Observable.just(types);
  }
}
