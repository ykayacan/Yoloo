package com.yoloo.android.data.repository.comment;

public class CommentRepositoryProvider {

  public static CommentRepository getRepository() {
    return CommentRepository.getInstance(CommentRemoteDataStore.getInstance(),
        CommentDiskDataStore.getInstance());
  }
}
