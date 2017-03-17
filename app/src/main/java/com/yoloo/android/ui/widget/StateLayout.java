package com.yoloo.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.yoloo.android.R;
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

  private static final int ANIMATION_DURATION = 250;

  private int loadingViewRes;
  private int emptyViewRes;
  private int errorViewRes;

  private int contentViewId;

  private View contentView;
  private View loadingView;
  private View errorView;
  private View emptyView;

  private boolean animateStateChange = false;

  @Nullable private OnStateChangedListener onStateChangedListener;

  private int currentViewState;

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

    contentViewId = a.getResourceId(R.styleable.StateLayout_contentViewId, NO_ID);

    final int viewState = a.getInt(R.styleable.StateLayout_initialViewState, VIEW_STATE_CONTENT);
    animateStateChange = a.getBoolean(R.styleable.StateLayout_animateStateChanges, false);

    a.recycle();

    setDefaultViewState(viewState);

    if (contentViewId == NO_ID) {
      throw new NullPointerException("app:contentViewId is null.");
    }

    loadingView = inflater.inflate(loadingViewRes, this, false);
    addView(loadingView);
    hideView(loadingView);

    errorView = inflater.inflate(errorViewRes, this, false);
    addView(errorView);
    hideView(errorView);

    emptyView = inflater.inflate(emptyViewRes, this, false);
    addView(emptyView);
    hideView(emptyView);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    contentView = ButterKnife.findById(this, contentViewId);
    hideView(contentView);
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

  private void setViewForState(View view, @ViewState int viewState) {
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
    }
  }

  private void dispatchStateChangeListener(View view, @ViewState int viewState) {
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

  private void showView(View view) {
    if (view instanceof ViewGroup) {
      final int childCount = ((ViewGroup) view).getChildCount();
      for (int i = 0; i < childCount; i++) {
        ((ViewGroup) view).getChildAt(i).setVisibility(VISIBLE);
      }
    }
    view.setVisibility(VISIBLE);
  }

  private void hideView(View view) {
    if (view instanceof ViewGroup) {
      final int childCount = ((ViewGroup) view).getChildCount();
      for (int i = 0; i < childCount; i++) {
        ((ViewGroup) view).getChildAt(i).setVisibility(GONE);
      }
    }
    view.setVisibility(GONE);
  }

  private void replaceView(View oldView, View newView) {
    if (oldView != null) {
      removeView(oldView);
    }

    addView(newView);
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
      default:
        return null;
    }
  }

  public boolean isAnimateLayoutChanges() {
    return animateStateChange;
  }

  /**
   * Sets whether an animate will occur when changing between {@link ViewState}
   *
   * @param animate the animate
   */
  public void setAnimateLayoutChanges(boolean animate) {
    animateStateChange = animate;
  }

  public void setState(@ViewState int toViewState) {
    final View currentView = getView(currentViewState);
    final View toView = getView(toViewState);

    showView(currentView);

    currentViewState = toViewState;

    if (animateStateChange) {
      currentView.animate()
          .alpha(0.0f)
          .setDuration(ANIMATION_DURATION)
          .withEndAction(() -> {
            hideView(currentView);
            removeView(currentView);

            showView(toView);
            toView.animate()
                .alpha(1.0f)
                .setDuration(ANIMATION_DURATION);
          });
    } else {
      hideView(currentView);
      showView(toView);
    }

    dispatchStateChangeListener(toView, toViewState);
  }

  /**
   * The interface View state.
   */
  @IntDef({
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
