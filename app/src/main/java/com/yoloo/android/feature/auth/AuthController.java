package com.yoloo.android.feature.auth;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.yoloo.android.R;
import com.yoloo.android.feature.auth.welcome.WelcomeController;
import com.yoloo.android.feature.base.BaseController;

public class AuthController extends BaseController {

  @BindView(R.id.layout_login_child) ViewGroup childContainer;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  public AuthController() {
  }

  public static AuthController create() {
    return new AuthController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_auth, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setChildRootController();
  }

  private void setChildRootController() {
    final Router childRouter = getChildRouter(childContainer).setPopsLastView(true);

    if (!childRouter.hasRootController()) {
      WelcomeController controller = WelcomeController.create();

      controller.addLifecycleListener(new LifecycleListener() {
        @Override
        public void onChangeStart(@NonNull Controller controller,
            @NonNull ControllerChangeHandler changeHandler,
            @NonNull ControllerChangeType changeType) {
          if (controller.isBeingDestroyed()) {
            getActivity().finish();
          }
        }
      });

      childRouter.setRoot(RouterTransaction.with(controller));
    }
  }
}
