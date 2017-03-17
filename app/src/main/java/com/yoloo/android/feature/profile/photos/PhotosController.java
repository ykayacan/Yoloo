package com.yoloo.android.feature.profile.photos;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yoloo.android.R;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.framework.MvpController;
import java.util.List;

public class PhotosController extends MvpController<PhotosView, PhotosPresenter>
    implements PhotosView {

  public static PhotosController create() {
    return new PhotosController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_photos, container, false);
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(List<MediaRealm> value) {

  }

  @Override public void onError(Throwable e) {

  }

  @Override public void onEmpty() {

  }

  @NonNull @Override public PhotosPresenter createPresenter() {
    return new PhotosPresenter();
  }
}
