package com.yoloo.android.data.repository.post;

public class PostRepositoryProvider {

  public static PostRepository getRepository() {
    return PostRepository.getInstance(PostRemoteDataStore.getInstance(),
        PostDiskDataStore.getInstance());
  }
}
