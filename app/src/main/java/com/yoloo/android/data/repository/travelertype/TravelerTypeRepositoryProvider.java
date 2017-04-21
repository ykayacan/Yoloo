package com.yoloo.android.data.repository.travelertype;

public class TravelerTypeRepositoryProvider {

  public static TravelerTypeRepository getRepository() {
    return TravelerTypeRepository.getInstance(TravelerTypeRemoteDataStore.getInstance());
  }
}
