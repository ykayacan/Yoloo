package com.yoloo.android.feature.editor.selectactionview;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseController;

public class SelectActionController extends BaseController {

  public static SelectActionController create() {
    return new SelectActionController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_select_action, container, false);
  }
}
