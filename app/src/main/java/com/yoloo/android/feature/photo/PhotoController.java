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
import butterknife.BindColor;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ViewUtil;
import uk.co.senab.photoview.PhotoView;

public class PhotoController extends BaseController {

  private static final String KEY_PHOTO_URL = "PHOTO_URL";

  @BindView(R.id.iv_photo) PhotoView ivPhoto;
  @BindView(R.id.toolbar_photo) Toolbar toolbar;

  @BindColor(R.color.primary_dark)
  int primaryDarkColor;

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

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    ViewUtil.setStatusBarColor(getActivity(), Color.BLACK);

    setHasOptionsMenu(true);
    setupToolbar();

    Glide.with(getActivity())
        .load(getArgs().getString(KEY_PHOTO_URL))
        .into(ivPhoto);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ViewUtil.setStatusBarColor(getActivity(), primaryDarkColor);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().popCurrentController();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }
}