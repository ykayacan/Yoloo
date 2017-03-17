package com.yoloo.android.feature.editor.bountyoverview;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
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
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.decoration.GridInsetItemDecoration;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.ViewUtils;
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
  private AccountRealm account;

  public static BountyController create() {
    return new BountyController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_bounty, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    ViewUtils.setStatusBarColor(getActivity(), primaryBlueColor);

    setHasOptionsMenu(true);
    setupRecyclerView();
    setupToolbar();

    DrawableHelper.create()
        .withDrawable(tvTotalBounty.getCompoundDrawables()[0])
        .withColor(getActivity(), android.R.color.white)
        .tint();

    final String[] bountyValues = getResources().getStringArray(R.array.label_editor_bounties);

    List<Drawable> drawables = new ArrayList<>(bountyValues.length);
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_1));
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_2));
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_3));
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_4));
    drawables.add(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_bounty_5));

    adapter.addAll(bountyValues, drawables);

    ControllerUtil.preventDefaultBackPressAction(view, () -> getPresenter().updateDraft(draft));
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        getRouter().handleBack();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @NonNull @Override public BountyPresenter createPresenter() {
    return new BountyPresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()
        ));
  }

  @Override public void onDraftAndAccountLoaded(PostRealm draft, AccountRealm account) {
    this.draft = draft;
    this.account = account;

    adapter.selectBountyItem(draft.getBounty());
    tvTotalBounty.setText(String.valueOf(this.account.getBountyCount() - this.draft.getBounty()));
  }

  @Override public void onDraftSaved() {
    getRouter().handleBack();
  }

  @Override public void onBountyClick(int value) {
    if (draft.getBounty() == 0) {
      if (isBountyQuantityValid(value)) {
        showBountyConfirmDialog(value);
      } else {
        adapter.clearSelection();
      }
    } else {
      final int tempTotal = Integer.parseInt(tvTotalBounty.getText().toString());
      tvTotalBounty.setText(String.valueOf(tempTotal + draft.getBounty()));
      draft.setBounty(0);
    }
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
    if (value > account.getBountyCount()) {
      Snackbar.make(getView(), R.string.error_editor_not_enough_bounty, Snackbar.LENGTH_SHORT)
          .show();
      return false;
    } else {
      return true;
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPost back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_editor_select_bounty_value);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void showBountyConfirmDialog(int value) {
    new AlertDialog.Builder(getActivity()).setCancelable(false)
        .setTitle(getResources().getString(R.string.label_editor_ensure_bounty, value))
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          draft.setBounty(value);
          getPresenter().updateDraft(draft);
        })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> adapter.clearSelection())
        .show();
  }
}
