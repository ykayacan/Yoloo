package com.yoloo.android.feature.photo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.ui.widget.ElasticDragDismissFrameLayout;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.VersionUtil;
import com.yoloo.android.util.ViewUtils;

import butterknife.BindColor;
import butterknife.BindView;
import uk.co.senab.photoview.PhotoView;

public class PhotoController extends BaseController {

  private static final String KEY_PHOTO_URL = "PHOTO_URL";

  @BindView(R.id.iv_photo) PhotoView ivPhoto;
  @BindView(R.id.toolbar_photo) Toolbar toolbar;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;

  public PhotoController(Bundle args) {
    super(args);
  }

  public static PhotoController create(String photoUrl) {
    final Bundle bundle = new BundleBuilder().putString(KEY_PHOTO_URL, photoUrl).build();

    return new PhotoController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_photo, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    ViewUtils.setStatusBarColor(getActivity(), Color.BLACK);

    if (VersionUtil.hasL()) {
      chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(getActivity()) {
        @Override public void onDragDismissed() {
          getRouter().popController(PhotoController.this);
        }
      };

      ((ElasticDragDismissFrameLayout) view).addListener(chromeFader);
    }

    setHasOptionsMenu(true);
    setupToolbar();

    if (VersionUtil.hasL()) {
      ivPhoto.setTransitionName("image.test");
    }

    Glide.with(getActivity())
        .load(getArgs().getString(KEY_PHOTO_URL))
        .into(ivPhoto);
  }

  @Override protected void onDestroyView(@NonNull View view) {
    super.onDestroyView(view);
    if (VersionUtil.hasL()) {
      ((ElasticDragDismissFrameLayout) view).removeListener(chromeFader);
    }
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      getRouter().handleBack();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPostToBeginning back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }
}
