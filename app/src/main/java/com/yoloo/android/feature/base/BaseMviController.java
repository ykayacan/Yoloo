package com.yoloo.android.feature.base;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.hannesdorfmann.mosby3.RestoreViewOnCreateMviController;
import com.hannesdorfmann.mosby3.mvi.MviPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

public abstract class BaseMviController<V extends MvpView, P extends MviPresenter<V, ?>>
    extends RestoreViewOnCreateMviController<V, P> {

  private Unbinder unbinder;

  protected BaseMviController() {
  }

  protected BaseMviController(Bundle args) {
    super(args);
  }

  protected abstract View inflateView(@NonNull LayoutInflater inflater,
      @NonNull ViewGroup container);

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
      @Nullable Bundle bundle) {
    final View view = inflateView(inflater, container);
    unbinder = ButterKnife.bind(this, view);
    onViewBound(view);
    return view;
  }

  @CallSuper
  protected void onViewBound(@NonNull View view) {

  }

  @CallSuper
  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    // Default settings to reset Activity UI state to each time a new controller is loaded.
    DrawerLayout drawerLayout = getDrawerLayout();
    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
  }

  @Override
  protected void onDestroyView(@NonNull View view) {
    super.onDestroyView(view);
    unbinder.unbind();
    unbinder = null;
  }

  protected ActionBar getSupportActionBar() {
    ActionBarInterface actionBarInterface = (ActionBarInterface) getActivity();
    return actionBarInterface.getSupportActionBar();
  }

  protected void setSupportActionBar(@NonNull Toolbar toolbar) {
    ActionBarInterface actionBarInterface = ((ActionBarInterface) getActivity());
    actionBarInterface.setSupportActionBar(toolbar);

    toolbar.setNavigationOnClickListener(v -> getRouter().handleBack());
  }

  protected DrawerLayout getDrawerLayout() {
    DrawerLayoutProvider drawerLayoutProvider = ((DrawerLayoutProvider) getActivity());
    return drawerLayoutProvider.getDrawerLayout();
  }

  protected NavigationView getNavigationView() {
    NavigationViewProvider navigationViewProvider = ((NavigationViewProvider) getActivity());
    return navigationViewProvider.getNavigationView();
  }
}
