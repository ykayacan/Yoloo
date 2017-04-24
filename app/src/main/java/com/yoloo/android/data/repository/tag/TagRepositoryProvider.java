package com.yoloo.android.data.repository.tag;

public class TagRepositoryProvider {

  public static TagRepository getRepository() {
    return TagRepository.getInstance(TagRemoteDataStore.getInstance(),
        TagDiskDataStore.getInstance());
  }
}
