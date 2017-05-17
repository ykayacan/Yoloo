package com.yoloo.android.feature.profile.photolist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.db.MediaRealm;
import com.yoloo.android.data.repository.media.MediaRepositoryProvider;
import com.yoloo.android.feature.fullscreenphoto.FullscreenPhotoController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.DisplayUtil;
import java.util.List;
import timber.log.Timber;

public class PhotoListController extends MvpController<PhotoListView, PhotoListPresenter>
    implements PhotoListView, OnItemClickListener<MediaRealm> {

  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.recycler_view) RecyclerView rvPhotos;

  private MediaEpoxyController epoxyController;

  public PhotoListController(@Nullable Bundle args) {
    super(args);
  }

  public static PhotoListController create(@NonNull String userId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_USER_ID, userId).build();

    return new PhotoListController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_photos, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setupRecyclerView();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    getPresenter().listMedias(getArgs().getString(KEY_USER_ID));
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(List<MediaRealm> value) {
    epoxyController.setData(value, false);
  }

  @Override
  public void onError(Throwable e) {
    Timber.e("onError(): %s", e.getMessage());
  }

  @Override
  public void onEmpty() {
    Timber.d("onEmpty()");
  }

  @NonNull
  @Override
  public PhotoListPresenter createPresenter() {
    return new PhotoListPresenter(MediaRepositoryProvider.getRepository());
  }

  @Override
  public void onItemClick(View v, MediaRealm item) {
    Controller controller = FullscreenPhotoController.create(item.getLargeSizeUrl());
    RouterTransaction transaction = RouterTransaction
        .with(controller)
        .pushChangeHandler(new FadeChangeHandler())
        .popChangeHandler(new FadeChangeHandler());

    getParentController().getRouter().pushController(transaction);
  }

  private void setupRecyclerView() {
    epoxyController = new MediaEpoxyController(this, Glide.with(getActivity()));

    rvPhotos.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    rvPhotos.addItemDecoration(new GridInsetItemDecoration(3, DisplayUtil.dpToPx(2), false));
    rvPhotos.setItemAnimator(new SlideInItemAnimator());
    rvPhotos.setHasFixedSize(true);
    rvPhotos.setAdapter(epoxyController.getAdapter());
  }
}
