package com.yoloo.android.feature.profile;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.base.framework.MvpView;
import java.util.List;

public interface ProfileView extends MvpView {

  void onProfileLoaded(AccountRealm account);

  void onPostsLoaded(Response<List<PostRealm>> response);

  void onError(Throwable t);
}
