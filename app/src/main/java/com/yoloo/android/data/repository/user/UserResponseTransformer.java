package com.yoloo.android.data.repository.user;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.backend.yolooApi.model.CollectionResponseAccount;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.util.Collections;
import java.util.List;

public class UserResponseTransformer
    implements ObservableTransformer<CollectionResponseAccount, Response<List<AccountRealm>>> {

  private UserResponseTransformer() {
    // empty constructor
  }

  public static UserResponseTransformer create() {
    return new UserResponseTransformer();
  }

  @Override
  public ObservableSource<Response<List<AccountRealm>>> apply(
      Observable<CollectionResponseAccount> upstream) {
    return upstream.map(
        response -> Response.create(getAccounts(response), response.getNextPageToken()));
  }

  private List<AccountRealm> getAccounts(CollectionResponseAccount response) {
    return response.getItems() == null
        ? Collections.emptyList()
        : Stream.of(response.getItems()).map(AccountRealm::new).toList();
  }
}
