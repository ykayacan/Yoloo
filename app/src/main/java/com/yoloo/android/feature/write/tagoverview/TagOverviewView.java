package com.yoloo.android.feature.write.tagoverview;

import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

public interface TagOverviewView extends MvpView {

  void onRecommendedTagsLoaded(List<TagRealm> tags);

  void onNewRecommendedTagsLoaded(List<TagRealm> tags);
}
