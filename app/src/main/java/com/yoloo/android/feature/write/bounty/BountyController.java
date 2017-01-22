package com.yoloo.android.feature.write.bounty;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindView;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.ui.recyclerview.GridInsetItemDecoration;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.VersionUtil;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class BountyController extends MvpController<BountyView, BountyPresenter>
    implements BountyView, OnBountyClickListener {

  @BindView(R.id.rv_bounty) RecyclerView rvBounty;

  @BindView(R.id.toolbar_bounty) Toolbar toolbar;

  @BindView(R.id.tv_total_bounty) TextView tvTotalBounty;

  @BindColor(R.color.primary_blue) int primaryBlueColor;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private BountyAdapter adapter;

  private PostRealm draft;

  private int totalBounty;
  private boolean isBountyConsumed;

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_bounty, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);

    setHasOptionsMenu(true);

    if (VersionUtil.hasL()) {
      getActivity().getWindow().setStatusBarColor(primaryBlueColor);
    }

    setupRecyclerView();
    setupToolbar();

    DrawableHelper.withContext(getActivity())
        .withColor(android.R.color.white)
        .withDrawable(tvTotalBounty.getCompoundDrawables()[0])
        .tint();

    final String[] bountyValues = getResources().getStringArray(R.array.label_bounties);

    List<Drawable> drawables = new ArrayList<>(bountyValues.length);
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_1));
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_2));
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_3));
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_4));
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_5));

    adapter.addAll(bountyValues, drawables);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);

    if (VersionUtil.hasL()) {
      getActivity().getWindow().setStatusBarColor(primaryDarkColor);
    }
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

  @NonNull @Override public BountyPresenter createPresenter() {
    return new BountyPresenter(UserRepository.getInstance(UserRemoteDataStore.getInstance(),
        UserDiskDataStore.getInstance()),
        PostRepository.getInstance(PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()));
  }

  @Override public void onBountyClick(int value) {
    if (isBountyConsumed) {
      draft.setBounty(0);
      getPresenter().updateBounty(totalBounty + value, 1);
      getPresenter().updateDraft(draft);
    } else {
      if (isBountyQuantityValid(value)) {
        showBountyConfirmDialog(value);
      } else {
        adapter.clearSelection();
      }
    }
  }

  @Override public void onDraftLoaded(PostRealm draft) {
    this.draft = draft;
    isBountyConsumed = draft.getBounty() != 0;
    adapter.selectBountyItem(draft.getBounty());
  }

  @Override public void onTotalBounty(int bounty) {
    totalBounty = bounty - draft.getBounty();
    tvTotalBounty.setText(String.valueOf(totalBounty));
  }

  @Override public void onBountyRenewed(int bounty) {
    isBountyConsumed = false;
    totalBounty = bounty;
    tvTotalBounty.setText(String.valueOf(bounty));
  }

  @Override public void onBountyConsumed(int bounties) {
    isBountyConsumed = true;
    totalBounty = bounties;
    tvTotalBounty.setText(String.valueOf(bounties));
  }

  @Override public void onError(Throwable t) {
    Timber.d(t);
  }

  private void setupRecyclerView() {
    adapter = new BountyAdapter(this);

    rvBounty.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    rvBounty.addItemDecoration(new GridInsetItemDecoration(2, 16, false));

    rvBounty.setItemAnimator(new DefaultItemAnimator());
    rvBounty.setHasFixedSize(true);
    rvBounty.setAdapter(adapter);
  }

  private boolean isBountyQuantityValid(int value) {
    if (value > totalBounty) {
      Snackbar.make(getView(), R.string.error_not_enough_bounty, Snackbar.LENGTH_SHORT).show();
      return false;
    } else {
      return true;
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(R.string.label_select_a_bounty);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
  }

  private void showBountyConfirmDialog(int value) {
    new AlertDialog.Builder(getActivity()).setCancelable(false)
        .setTitle(getResources().getString(R.string.label_ensure_bounty, value))
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          draft.setBounty(value);
          getPresenter().updateBounty(totalBounty - value, -1);
          getPresenter().updateDraft(draft);
        })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> adapter.clearSelection())
        .show();
  }
}
