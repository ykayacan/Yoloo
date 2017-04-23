package com.yoloo.backend.media;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.googlecode.objectify.Key;
import com.yoloo.backend.util.StringUtil;
import ix.Ix;
import java.util.Collection;
import java.util.Collections;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class MediaService {

  private static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();

  public void deleteMedia(Key<MediaEntity> mediaKey) {
    deleteMedias(Collections.singleton(mediaKey));
  }

  public void deleteMedias(Collection<Key<MediaEntity>> mediaKeys) {
    Ix.from(mediaKeys)
        .map(mediaKey -> StringUtil.split(mediaKey.getName(), "/"))
        .map(s -> BlobId.of(s.get(0), s.get(1)))
        .foreach(blobId -> STORAGE.batch().delete(blobId));
  }
}