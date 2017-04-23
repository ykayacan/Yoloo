package com.yoloo.android.feature.fullscreenphoto;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.ui.widget.ElasticDragDismissFrameLayout;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.VersionUtil;
import com.yoloo.android.util.ViewUtils;

public class FullscreenPhotoController extends BaseController {

  private static final String KEY_PHOTO_URL = "PHOTO_URL";
  private static final String KEY_MEDIA_ID = "MEDIA_ID";

  @BindView(R.id.iv_photo) PhotoView ivPhoto;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;

  public FullscreenPhotoController(Bundle args) {
    super(args);
  }

  public static FullscreenPhotoController create(String photoUrl, String mediaId) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_PHOTO_URL, photoUrl)
        .putString(KEY_MEDIA_ID, mediaId)
        .build();

    return new FullscreenPhotoController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_photo, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    if (VersionUtil.hasL()) {
      chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(getActivity()) {
        @Override
        public void onDragDismissed() {
          getRouter().popController(FullscreenPhotoController.this);
        }
      };

      ((ElasticDragDismissFrameLayout) view).addListener(chromeFader);
    }

    setupToolbar();

    if (VersionUtil.hasL()) {
      ivPhoto.setTransitionName("transition." + getArgs().getString(KEY_MEDIA_ID));
    }

    Glide.with(getActivity()).load(getArgs().getString(KEY_PHOTO_URL)).into(ivPhoto);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ViewUtils.setStatusBarColor(getActivity(), Color.BLACK);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    ViewUtils.setStatusBarColor(getActivity(), Color.TRANSPARENT);
  }

  @Override
  protected void onDestroyView(@NonNull View view) {
    super.onDestroyView(view);
    if (VersionUtil.hasL()) {
      ((ElasticDragDismissFrameLayout) view).removeListener(chromeFader);
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }
}
