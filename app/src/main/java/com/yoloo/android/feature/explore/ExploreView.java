package com.yoloo.android.feature.explore;

import com.yoloo.android.framework.MvpView;
import java.util.List;

interface ExploreView extends MvpView {

  void onDataLoaded(List<ExploreItem<?>> items);
}
