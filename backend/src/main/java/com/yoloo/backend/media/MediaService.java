package com.yoloo.backend.media;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.googlecode.objectify.Key;
import com.yoloo.backend.util.StringUtil;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class MediaService {

  public static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();

  public void delete(Key<Media> mediaKey) {
    List<String> splitted = StringUtil.splitToList(mediaKey.getName(), "/");
    STORAGE.delete(BlobId.of(splitted.get(0), splitted.get(1)));
  }
}