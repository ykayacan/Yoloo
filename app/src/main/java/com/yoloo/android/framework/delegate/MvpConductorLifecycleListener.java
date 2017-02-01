package com.yoloo.android.framework.delegate;

import android.support.annotation.NonNull;
import android.view.View;
import com.bluelinelabs.conductor.Controller;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.framework.MvpView;
import com.yoloo.android.util.Preconditions;

/**
 * This {@link Controller.LifecycleListener} has to added to your Controller to integrate Mosby.
 * This class uses {@link MvpConductorDelegateCallback} to call Mosby's methods like {@code
 * createPresenter()} according to the {@link Controller} lifecycle events.
 *
 * <p> {@link MvpController} already implements {@link MvpConductorDelegateCallback} and registers
 * this class as listener. </p>
 *
 * @author Hannes Dorfmann
 * @see MvpConductorDelegateCallback
 * @see MvpController
 * @since 1.0
 */
public class MvpConductorLifecycleListener<V extends MvpView, P extends MvpPresenter<V>>
    extends Controller.LifecycleListener {

  protected final MvpConductorDelegateCallback<V, P> delegateCallback;

  /**
   * Instantiate a new Mosby MVP Listener
   *
   * @param delegateCallback {@link MvpConductorDelegateCallback} to set presenter. Typically the
   * controller himself.
   */
  public MvpConductorLifecycleListener(MvpConductorDelegateCallback<V, P> delegateCallback) {
    this.delegateCallback =
        Preconditions.checkNotNull(delegateCallback, "MvpConductorDelegateCallback is null!");
  }

  protected MvpConductorDelegateCallback<V, P> getDelegateCallback() {
    return delegateCallback;
  }

  @Override public void postCreateView(@NonNull Controller controller, @NonNull View view) {
    P presenter = delegateCallback.getPresenter();
    if (presenter == null) {
      presenter = delegateCallback.createPresenter();
      Preconditions.checkNotNull(presenter,
          "Presenter returned from createPresenter() is null in " + delegateCallback);
      delegateCallback.setPresenter(presenter);
    }

    V mvpView = delegateCallback.getMvpView();
    Preconditions.checkNotNull(mvpView,
        "MVP View returned from getMvpView() is null in " + delegateCallback);
    presenter.onAttachView(mvpView);
  }

  @Override public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
    P presenter = getDelegateCallback().getPresenter();
    Preconditions.checkNotNull(presenter,
        "Presenter returned from getPresenter() is null in " + delegateCallback);
    presenter.onDetachView();
  }
}
