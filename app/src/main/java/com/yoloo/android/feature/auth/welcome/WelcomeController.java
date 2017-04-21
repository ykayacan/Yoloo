package com.yoloo.android.feature.auth.welcome;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.feature.auth.selecttype.SelectTypeController;
import com.yoloo.android.feature.auth.signin.SignInController;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.ui.changehandler.CircularRevealChangeHandlerCompat;
import com.yoloo.android.util.HtmlUtil;

public class WelcomeController extends BaseController {

  @BindView(R.id.tv_login_action_login) TextView tvActionLogin;
  @BindView(R.id.videoview_login_background) VideoView videoView;

  @BindString(R.string.action_login_already_have_account) String alreadyHaveAccountString;

  private View decorView;

  private int position = 0;

  public static WelcomeController create() {
    return new WelcomeController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_welcome, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    decorView = getActivity().getWindow().getDecorView();

    final String path =
        "android.resource://" + getActivity().getPackageName() + "/" + R.raw.yoloo_login;

    videoView.setVideoURI(Uri.parse(path));
    videoView.setOnPreparedListener(mp -> {
      videoView.start();
      mp.setLooping(true);
    });

    tvActionLogin.setText(HtmlUtil.fromHtml(alreadyHaveAccountString));
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    hideSystemUI();
    if (position != 0) {
      videoView.seekTo(position);
      videoView.start();
    }
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    showSystemUI();
    videoView.pause();
    position = videoView.getCurrentPosition();
  }

  @Override
  protected void onDestroyView(@NonNull View view) {
    videoView.suspend();
    super.onDestroyView(view);
  }

  @OnClick(R.id.btn_login_start_using)
  void startUsing(View view) {
    startTransaction(SelectTypeController.create(),
        new CircularRevealChangeHandlerCompat(view, view.getRootView()));
  }

  @OnClick(R.id.tv_login_action_login)
  void login() {
    startTransaction(SignInController.create(), new HorizontalChangeHandler());
  }

  private void startTransaction(Controller controller, ControllerChangeHandler handler) {
    RouterTransaction rt =
        RouterTransaction.with(controller).pushChangeHandler(handler).popChangeHandler(handler);

    getRouter().pushController(rt);
  }

  // This snippet hides the system bars.
  private void hideSystemUI() {
    // Set the IMMERSIVE flag.
    // Set the content to appear under the system bars so that the content
    // doesn't resize when the system bars hide and show.
    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }

  // This snippet shows the system bars. It does this by removing all the flags
  // except for the ones that make the content appear under the system bars.
  private void showSystemUI() {
    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
  }
}
