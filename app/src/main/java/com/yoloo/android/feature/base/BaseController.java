package com.yoloo.android.feature.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;

public abstract class BaseController extends Controller {

  private Unbinder unbinder;
  private boolean hasExited;

  protected BaseController() {
  }

  protected BaseController(Bundle args) {
    super(args);
  }

  protected abstract View inflateView(@NonNull LayoutInflater inflater,
      @NonNull ViewGroup container);

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    final View view = inflateView(inflater, container);
    unbinder = ButterKnife.bind(this, view);
    onViewCreated(view);
    return view;
  }

  protected void onViewCreated(@NonNull View view) {
  }

  @Override
  protected void onDestroyView(@NonNull View view) {
    super.onDestroyView(view);
    unbinder.unbind();
    unbinder = null;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (hasExited) {
      //YolooApp.getRefWatcher(getActivity()).watch(this);
    }
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);

    hasExited = !changeType.isEnter;
    if (isDestroyed()) {
      //YolooApp.getRefWatcher(getActivity()).watch(this);
    }
  }

  @Nullable
  protected ActionBar getSupportActionBar() {
    return getActivity() != null ? ((AppCompatActivity) getActivity()).getSupportActionBar() : null;
  }

  protected void setSupportActionBar(@NonNull Toolbar toolbar) {
    if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
      ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }
  }
}