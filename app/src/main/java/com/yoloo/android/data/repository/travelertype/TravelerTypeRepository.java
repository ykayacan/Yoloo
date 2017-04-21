package com.yoloo.android.data.repository.travelertype;

import com.yoloo.android.data.model.TravelerType;
import io.reactivex.Observable;
import java.util.List;

public class TravelerTypeRepository {

  private static TravelerTypeRepository instance;

  private final TravelerTypeRemoteDataStore remoteDataStore;

  private TravelerTypeRepository(TravelerTypeRemoteDataStore remoteDataStore) {
    this.remoteDataStore = remoteDataStore;
  }

  public static TravelerTypeRepository getInstance(TravelerTypeRemoteDataStore remoteDataStore) {
    if (instance == null) {
      instance = new TravelerTypeRepository(remoteDataStore);
    }
    return instance;
  }

  public Observable<List<TravelerType>> listTravelerTypes() {
    return remoteDataStore.listTravelerTypes();
  }
}
