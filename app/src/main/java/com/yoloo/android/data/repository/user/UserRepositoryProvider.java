package com.yoloo.android.data.repository.user;

public class UserRepositoryProvider {

  public static UserRepository getRepository() {
    return UserRepository.getInstance(UserRemoteDataStore.getInstance(),
        UserDiskDataStore.getInstance());
  }
}
