package com.yoloo.android.data.repository.group;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponseTravelerGroupEntity;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.util.Collections;
import java.util.List;

public class GroupResponseTransformer implements
    ObservableTransformer<CollectionResponseTravelerGroupEntity, Response<List<GroupRealm>>> {

  private GroupResponseTransformer() {
    // empty constructor
  }

  public static GroupResponseTransformer create() {
    return new GroupResponseTransformer();
  }

  @Override
  public ObservableSource<Response<List<GroupRealm>>> apply(
      Observable<CollectionResponseTravelerGroupEntity> upstream) {
    return upstream.map(
        response -> Response.create(getGroups(response), response.getNextPageToken()));
  }

  private List<GroupRealm> getGroups(CollectionResponseTravelerGroupEntity response) {
    return response.getItems() == null
        ? Collections.emptyList()
        : Stream.of(response.getItems()).map(GroupRealm::new).toList();
  }
}
