package com.yoloo.android.data.repository.user.datasource;

import com.yoloo.android.data.ApiManager;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.faker.FakerUtil;
import com.yoloo.android.data.model.AccountRealm;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class UserRemoteDataStore {

  private static UserRemoteDataStore INSTANCE;

  private UserRemoteDataStore() {
  }

  public static UserRemoteDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new UserRemoteDataStore();
    }
    return INSTANCE;
  }

  public Single<AccountRealm> get(String userId) {
    return ApiManager.getIdToken()
        .doOnSuccess(s -> Timber.d("Token: %s", s))
        .flatMap(s -> Single.just(new AccountRealm()));
  }

  public Single<AccountRealm> getMe() {
    return ApiManager.getIdToken()
        .doOnSuccess(s -> Timber.d("Token: %s", s))
        .flatMap(s -> Single.just(new AccountRealm()));
  }

  public Single<AccountRealm> add(AccountRealm account) {
    return ApiManager.getIdToken()
        .doOnSuccess(s -> Timber.d("Token: %s", s))
        .flatMap(s -> Single.just(account));
    /*return Single
        .fromCallable(() ->
            ApiManager.INSTANCE.getApi().accounts()
                .register(account.getGender(), account.getLocale(), account.getCategoryIds())
                .setRequestHeaders(new HttpHeaders().setAuthorization(getIdToken()))
                .execute())
        .map(AccountRealm::new);*/
  }

  public Observable<Response<List<AccountRealm>>> list(String name, String cursor, int limit) {
    AccountRealm a1 = new AccountRealm()
        .setUsername("sia")
        .setAvatarUrl(FakerUtil.getAvatarRandomUrl());

    AccountRealm a2 = new AccountRealm()
        .setUsername("sinan")
        .setAvatarUrl(FakerUtil.getAvatarRandomUrl());

    AccountRealm a3 = new AccountRealm()
        .setUsername("sinay")
        .setAvatarUrl(FakerUtil.getAvatarRandomUrl());

    List<AccountRealm> accounts = new ArrayList<>(3);
    accounts.add(a1);
    accounts.add(a2);
    accounts.add(a3);

    return Observable.just(Response.create(accounts, null, null));
  }
}