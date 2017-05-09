package com.yoloo.android.data.repository.media;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.MediaRealm;
import io.reactivex.Observable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MediaRepository {

  private static MediaRepository instance;

  private final MediaRemoteDataStore remoteDataStore;
  private final MediaDiskDataStore diskDataStore;

  private MediaRepository(MediaRemoteDataStore remoteDataStore, MediaDiskDataStore diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static MediaRepository getInstance(MediaRemoteDataStore remoteDataStore,
      MediaDiskDataStore diskDataStore) {
    if (instance == null) {
      instance = new MediaRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  public Observable<Response<List<MediaRealm>>> listMedias(@Nonnull String userId,
      @Nullable String cursor, int limit) {
    return remoteDataStore.list(userId, cursor, limit);
  }

  public Observable<Response<List<MediaRealm>>> listRecentMedias(@Nullable String cursor,
      int limit) {
    return remoteDataStore.listRecentMedias(cursor, limit);
  }
}
