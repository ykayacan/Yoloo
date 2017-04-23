package com.yoloo.android.data.repository.media;

public final class MediaRepositoryProvider {
  public static MediaRepository getRepository() {
    return MediaRepository.getInstance(MediaRemoteDataStore.getInstance(),
        MediaDiskDataStore.getInstance());
  }
}
