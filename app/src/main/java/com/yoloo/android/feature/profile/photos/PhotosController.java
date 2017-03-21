package com.yoloo.android.feature.profile.photos;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.yoloo.android.R;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.data.repository.media.MediaRepository;
import com.yoloo.android.data.repository.media.datasource.MediaDiskDataStore;
import com.yoloo.android.data.repository.media.datasource.MediaRemoteDataStore;
import com.yoloo.android.feature.photo.PhotoController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.changehandler.SharedElementDelayingChangeHandler;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.DisplayUtil;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class PhotosController extends MvpController<PhotosView, PhotosPresenter>
    implements PhotosView, OnItemClickListener<MediaRealm> {

  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.rv_photos) RecyclerView rvPhotos;

  private PhotosAdapter adapter;

  public PhotosController(@Nullable Bundle args) {
    super(args);
  }

  public static PhotosController create(@NonNull String userId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_USER_ID, userId).build();

    return new PhotosController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_photos, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    setupRecyclerView();
  }

  @Override protected void onAttach(@NonNull View view) {
    getPresenter().listMedias(getArgs().getString(KEY_USER_ID));
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(List<MediaRealm> value) {
    adapter.addMedias(value);
  }

  @Override public void onError(Throwable e) {
    Timber.e("onError(): %s", e.getMessage());
  }

  @Override public void onEmpty() {
    Timber.d("onEmpty()");
  }

  @NonNull @Override public PhotosPresenter createPresenter() {
    return new PhotosPresenter(
        MediaRepository.getInstance(
            MediaRemoteDataStore.getInstance(),
            MediaDiskDataStore.getInstance())
    );
  }

  @Override public void onItemClick(View v, EpoxyModel<?> model, MediaRealm item) {
    Timber.d("onItemClick(): ", item.toString());
    String name = "image.test";
    List<String> names = Collections.singletonList(name);

    Controller controller = PhotoController.create(item.getLargeSizeUrl());
    RouterTransaction transaction = RouterTransaction.with(controller)
        .pushChangeHandler(new SharedElementDelayingChangeHandler(names))
        .popChangeHandler(new SharedElementDelayingChangeHandler(names));

    getParentController().getRouter().pushController(transaction);
  }

  private void setupRecyclerView() {
    adapter = new PhotosAdapter(this);

    rvPhotos.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    rvPhotos.addItemDecoration(new GridInsetItemDecoration(3, DisplayUtil.dpToPx(2), false));
    SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvPhotos.setItemAnimator(animator);
    rvPhotos.setHasFixedSize(true);
    rvPhotos.setAdapter(adapter);
  }
}
