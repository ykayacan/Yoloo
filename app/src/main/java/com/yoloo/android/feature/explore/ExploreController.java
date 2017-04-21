package com.yoloo.android.feature.explore;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yoloo.android.framework.MvpController;

public class ExploreController extends MvpController<ExploreView, ExplorePresenter>
    implements ExploreView {

  @NonNull
  @Override
  public ExplorePresenter createPresenter() {
    return new ExplorePresenter();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return null;
  }
}
