package com.yoloo.android.data.repository.group;

public final class GroupRepositoryProvider {
  public static GroupRepository getRepository() {
    return GroupRepository.getInstance(GroupRemoteDataStore.getInstance(),
        GroupDiskDataStore.getInstance());
  }
}
