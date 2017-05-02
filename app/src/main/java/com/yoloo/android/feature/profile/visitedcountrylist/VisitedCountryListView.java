package com.yoloo.android.feature.profile.visitedcountrylist;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CountryRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

public interface VisitedCountryListView extends MvpDataView<List<CountryRealm>> {
  void onMeUpdated(AccountRealm me);
}
