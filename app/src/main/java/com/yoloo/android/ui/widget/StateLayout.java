package com.yoloo.android.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.yoloo.android.R;
import com.yoloo.android.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * View that contains 4 different states: Content, Error, Empty, and Loading.<br>
 * Each state has their own separate layout which can be shown/hidden by setting
 * the {@link ViewState} accordingly
 * Every MultiStateView <b><i>MUST</i></b> contain a content view. The content view
 * is obtained from whatever is inside of the tags of the view via its XML declaration
 */
public class StateLayout extends CoordinatorLayout {

  /**
   * The constant VIEW_STATE_UNKNOWN.
   */
  public static final int VIEW_STATE_UNKNOWN = -1;
  /**
   * The constant VIEW_STATE_CONTENT.
   */
  public static final int VIEW_STATE_CONTENT = 0;
  /**
   * The constant VIEW_STATE_ERROR.
   */
  public static final int VIEW_STATE_ERROR = 1;
  /**
   * The constant VIEW_STATE_EMPTY.
   */
  public static final int VIEW_STATE_EMPTY = 2;
  /**
   * The constant VIEW_STATE_LOADING.
   */
  public static final int VIEW_STATE_LOADING = 3;

  private int loadingViewRes;
  private int emptyViewRes;
  private int errorViewRes;

  private View contentView;
  private View loadingView;
  private View errorView;
  private View emptyView;

  private boolean animateStateChanges = false;

  @Nullable private OnStateChangedListener onStateChangedListener;

  @ViewState private int currentViewState;
  @ViewState private int previousViewState;

  private LayoutInflater inflater;

  public StateLayout(Context context) {
    super(context, null);
    init(context, null, 0);
  }

  public StateLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public StateLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  private void init(Context context, AttributeSet attrs, int defStyle) {
    inflater = LayoutInflater.from(getContext());

    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StateLayout);

    loadingViewRes = a.getResourceId(R.styleable.StateLayout_loadingLayout, 0);
    emptyViewRes = a.getResourceId(R.styleable.StateLayout_emptyLayout, 0);
    errorViewRes = a.getResourceId(R.styleable.StateLayout_errorLayout, 0);

    final int viewState = a.getInt(R.styleable.StateLayout_initialViewState, VIEW_STATE_CONTENT);
    animateStateChanges = a.getBoolean(R.styleable.StateLayout_animateStateChanges, false);

    a.recycle();

    setDefaultViewState(viewState);
  }

  private void setDefaultViewState(int viewState) {
    switch (viewState) {
      case VIEW_STATE_CONTENT:
        this.currentViewState = VIEW_STATE_CONTENT;
        break;
      case VIEW_STATE_ERROR:
        this.currentViewState = VIEW_STATE_ERROR;
        break;
      case VIEW_STATE_EMPTY:
        this.currentViewState = VIEW_STATE_EMPTY;
        break;
      case VIEW_STATE_LOADING:
        this.currentViewState = VIEW_STATE_LOADING;
        break;
      case VIEW_STATE_UNKNOWN:
      default:
        this.currentViewState = VIEW_STATE_UNKNOWN;
        break;
    }
  }

  /**
   * <p>Specify content view with the given id</p>
   *
   * @param layoutRes The id to specify
   * @return This StateLayout object to allow for chaining of calls to set methods
   */
  public StateLayout setContentViewRes(@LayoutRes int layoutRes) {
    setContentView(inflater.inflate(layoutRes, this, false));
    return this;
  }

  /**
   * <p>Set content view.</p>
   *
   * @param contentView The content view to add
   * @return This StateLayout object to allow for chaining of calls to set methods
   */
  public StateLayout setContentView(View contentView) {
    setViewForState(contentView, VIEW_STATE_CONTENT);
    return this;
  }

  /**
   * <p>Specify empty view with the given id</p>
   *
   * @param layoutRes The id to specify
   * @return This StateLayout object to allow for chaining of calls to set methods
   */
  public StateLayout setEmptyViewRes(@LayoutRes int layoutRes) {
    setEmptyView(inflater.inflate(layoutRes, this, false));
    return this;
  }

  /**
   * <p>Set empty view.</p>
   *
   * @param emptyView The empty view to add
   * @return This StateLayout object to allow for chaining of calls to set methods
   */
  public StateLayout setEmptyView(View emptyView) {
    setViewForState(emptyView, VIEW_STATE_EMPTY);
    return this;
  }

  /**
   * <p>Specify error view with the given id</p>
   *
   * @param layoutRes The id to specify
   * @return This StateLayout object to allow for chaining of calls to set methods
   */
  public StateLayout setErrorViewRes(@LayoutRes int layoutRes) {
    setErrorView(inflater.inflate(layoutRes, this, false));
    return this;
  }

  /**
   * <p>set error view.</p>
   *
   * @param errorView the error view to add
   * @return This StateLayout object to allow for chaining of calls to set methods
   */
  public StateLayout setErrorView(View errorView) {
    setViewForState(errorView, VIEW_STATE_ERROR);
    return this;
  }

  /**
   * <p>Specify loading view with the given id</p>
   *
   * @param layoutRes The id to specify
   * @return This StateLayout object to allow for chaining of calls to set methods
   */
  public StateLayout setLoadingViewRes(@LayoutRes int layoutRes) {
    setLoadingView(View.inflate(getContext(), layoutRes, this));
    return this;
  }

  /**
   * <p>Set loading view.</p>
   *
   * @param loadingView the loading view to add
   * @return This StateLayout object to allow for chaining of calls to set methods
   */
  public StateLayout setLoadingView(View loadingView) {
    setViewForState(loadingView, VIEW_STATE_LOADING);
    return this;
  }

  public void setViewForState(View view, @ViewState int viewState) {
    switch (viewState) {
      case VIEW_STATE_CONTENT:
        contentView = view;
        break;
      case VIEW_STATE_EMPTY:
        replaceView(emptyView, view);
        emptyView = view;
        break;
      case VIEW_STATE_ERROR:
        replaceView(errorView, view);
        errorView = view;
        break;
      case VIEW_STATE_LOADING:
        replaceView(loadingView, view);
        loadingView = view;
        break;
      case VIEW_STATE_UNKNOWN:
        break;
    }
  }

  public void setState(@ViewState int viewState) {
    if (currentViewState == viewState) {
      return;
    }

    switch (viewState) {
      case VIEW_STATE_CONTENT:
        changeStates(viewState);
        showContentView();
        break;
      case VIEW_STATE_EMPTY:
        changeStates(viewState);
        showEmptyView();
        break;
      case VIEW_STATE_ERROR:
        changeStates(viewState);
        showErrorView();
        break;
      case VIEW_STATE_LOADING:
        changeStates(viewState);
        showLoadingView();
        break;
      case VIEW_STATE_UNKNOWN:
        break;
    }
  }

  private void changeStates(@ViewState int viewState) {
    previousViewState = currentViewState;
    currentViewState = viewState;
  }

  private void ensureLoadingView() {
    if (loadingView == null && loadingViewRes != 0) {
      loadingView = inflater.inflate(loadingViewRes, this, false);
      addView(loadingView, loadingView.getLayoutParams());
    }
  }

  private void ensureContentView() {
    Preconditions.checkNotNull(contentView, "ContentView can not be empty!");
  }

  private void ensureErrorView() {
    if (errorView == null && errorViewRes != 0) {
      errorView = inflater.inflate(errorViewRes, this, false);
      addView(errorView, errorView.getLayoutParams());
    }
  }

  private void ensureEmptyView() {
    if (emptyView == null && emptyViewRes != 0) {
      emptyView = inflater.inflate(emptyViewRes, this, false);
      addView(emptyView, emptyView.getLayoutParams());
    }
  }

  private void showContentView() {
    ensureContentView();

    if (animateStateChanges) {
      animateLayoutChange(getView(previousViewState));
    } else {
      showView(contentView);
      hideView(emptyView);
      hideView(errorView);
      hideView(loadingView);
    }

    checkStateChangeListener(contentView, VIEW_STATE_CONTENT);
  }

  private void showEmptyView() {
    ensureEmptyView();

    if (animateStateChanges) {
      animateLayoutChange(getView(previousViewState));
    } else {
      showView(emptyView);
      hideView(contentView);
      hideView(errorView);
      hideView(loadingView);
    }

    checkStateChangeListener(emptyView, VIEW_STATE_EMPTY);
  }

  private void showErrorView() {
    ensureErrorView();

    if (animateStateChanges) {
      animateLayoutChange(getView(previousViewState));
    } else {
      showView(errorView);
      hideView(contentView);
      hideView(emptyView);
      hideView(loadingView);
    }

    checkStateChangeListener(errorView, VIEW_STATE_ERROR);
  }

  private void showLoadingView() {
    ensureLoadingView();

    if (animateStateChanges) {
      animateLayoutChange(getView(previousViewState));
    } else {
      showView(loadingView);
      hideView(contentView);
      hideView(emptyView);
      hideView(errorView);
    }

    checkStateChangeListener(loadingView, VIEW_STATE_LOADING);
  }

  private void checkStateChangeListener(View view, @ViewState int viewState) {
    if (onStateChangedListener != null) {
      onStateChangedListener.onStatedChange(view, viewState);
    }
  }

  /**
   * Sets state listener.
   *
   * @param onStateChangedListener the on state changed listener
   */
  public void setViewStateListener(@Nullable OnStateChangedListener onStateChangedListener) {
    this.onStateChangedListener = onStateChangedListener;
  }

  /**
   * Animates the layout changes between {@link ViewState}
   *
   * @param previousView The view that it was currently on
   */
  private void animateLayoutChange(@Nullable final View previousView) {
    if (previousView == null) {
      getView(currentViewState).setVisibility(View.VISIBLE);
      return;
    }

    previousView.setVisibility(View.VISIBLE);
    previousView.animate()
        .alpha(0.0f)
        .setDuration(250L)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            previousView.setVisibility(View.GONE);
            View current = getView(currentViewState);
            current.setVisibility(VISIBLE);
            current.animate()
                .alpha(1.0f)
                .setDuration(250L);
          }
        });
  }

  /**
   * first init and call one of
   * {@link #setContentView(View)}
   * {@link #setEmptyView(View)}
   * {@link #setErrorView(View)}
   * {@link #setLoadingView(View)} ,you must call it to init state.
   */
  public void initWithState(@ViewState int state) {
    if (state == currentViewState) { // default view state
      showContentView();
    } else {
      setState(state);
    }
  }

  private void showView(View view) {
    if (view != null) {
      view.setVisibility(VISIBLE);
    }
  }

  private void hideView(View view) {
    if (view != null) {
      view.setVisibility(GONE);
    }
  }

  private void replaceView(View oldView, View newView) {
    if (oldView != null) {
      removeView(oldView);
    }

    addView(newView, newView.getLayoutParams());
  }

  @Nullable private View getView(@ViewState int viewState) {
    switch (viewState) {
      case VIEW_STATE_CONTENT:
        return contentView;
      case VIEW_STATE_EMPTY:
        return emptyView;
      case VIEW_STATE_ERROR:
        return errorView;
      case VIEW_STATE_LOADING:
        return loadingView;
      case VIEW_STATE_UNKNOWN:
      default:
        return null;
    }
  }

  /**
   * Sets whether an animate will occur when changing between {@link ViewState}
   *
   * @param animate the animate
   */
  public void setAnimateLayoutChanges(boolean animate) {
    animateStateChanges = animate;
  }

  /**
   * The interface View state.
   */
  @IntDef({
      VIEW_STATE_UNKNOWN,
      VIEW_STATE_CONTENT,
      VIEW_STATE_ERROR,
      VIEW_STATE_EMPTY,
      VIEW_STATE_LOADING
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface ViewState {

  }

  public interface OnStateChangedListener {

    /**
     * Callback for when the {@link ViewState} has changed
     *
     * @param viewState The {@link ViewState} that was switched to
     */
    void onStatedChange(View stateView, @ViewState int viewState);
  }
}
