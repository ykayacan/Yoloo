package com.yoloo.android.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yoloo.android.R;
import com.yoloo.android.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import timber.log.Timber;

/**
 * View that contains 4 different states: Content, Error, Empty, and Loading.<br>
 * Each state has their own separate layout which can be shown/hidden by setting
 * the {@link ViewState} accordingly
 * Every MultiStateView <b><i>MUST</i></b> contain a content view. The content view
 * is obtained from whatever is inside of the tags of the view via its XML declaration
 */
public class MultiStateLayout extends CoordinatorLayout {

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

  private static final String TAG_EMPTY = "empty";
  private static final String TAG_LOADING = "loading";
  private static final String TAG_ERROR = "error";

  private int loadingViewResId;
  private int emptyViewResId;
  private int errorViewResId;

  private View contentView;
  private View loadingView;
  private View errorView;
  private View emptyView;

  private boolean animateViewChanges = false;

  @Nullable private OnStateChangedListener onStateChangedListener;
  @Nullable private OnStateInflatedListener onStateInflatedListener;

  @ViewState private int viewState;

  /**
   * Instantiates a new Multi state view.
   *
   * @param context the context
   */
  public MultiStateLayout(Context context) {
    super(context, null);
    init(context, null);
  }

  /**
   * Instantiates a new Multi state view.
   *
   * @param context the context
   * @param attrs the attrs
   */
  public MultiStateLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  /**
   * Instantiates a new Multi state view.
   *
   * @param context the context
   * @param attrs the attrs
   * @param defStyle the def style
   */
  public MultiStateLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiStateLayout);

    loadingViewResId = a.getResourceId(R.styleable.MultiStateLayout_msv_loadingView, -1);
    emptyViewResId = a.getResourceId(R.styleable.MultiStateLayout_msv_emptyView, -1);
    errorViewResId = a.getResourceId(R.styleable.MultiStateLayout_msv_errorView, -1);

    final int viewState = a.getInt(R.styleable.MultiStateLayout_msv_viewState, VIEW_STATE_CONTENT);
    animateViewChanges = a.getBoolean(R.styleable.MultiStateLayout_msv_animateViewChanges, false);

    switch (viewState) {
      case VIEW_STATE_CONTENT:
        this.viewState = VIEW_STATE_CONTENT;
        break;
      case VIEW_STATE_ERROR:
        this.viewState = VIEW_STATE_ERROR;
        break;
      case VIEW_STATE_EMPTY:
        this.viewState = VIEW_STATE_EMPTY;
        break;
      case VIEW_STATE_LOADING:
        this.viewState = VIEW_STATE_LOADING;
        break;
      case VIEW_STATE_UNKNOWN:
      default:
        this.viewState = VIEW_STATE_UNKNOWN;
        break;
    }

    a.recycle();
  }

  /*@Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (contentView == null) {
      throw new IllegalArgumentException("Content view is not defined");
    }
    setView(VIEW_STATE_UNKNOWN);
  }*/

  /*@Override
  protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
    if (isValidContentView(child)) {
      contentView = child;
    }
    return super.addViewInLayout(child, index, params);
  }*/

  /*@Override protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params,
      boolean preventRequestLayout) {
    if (isValidContentView(child)) {
      contentView = child;
    }
    return super.addViewInLayout(child, index, params, preventRequestLayout);
  }*/

  /**
   * Returns the {@link View} associated with the {@link ViewState}
   *
   * @param state The {@link ViewState} with to return the view for
   * @return The {@link View} associated with the {@link ViewState}, null if no view is present
   */
  @Nullable public View getView(@ViewState int state) {
    switch (state) {
      case VIEW_STATE_LOADING:
        ensureLoadingView();
        return loadingView;
      case VIEW_STATE_CONTENT:
        return contentView;
      case VIEW_STATE_EMPTY:
        ensureEmptyView();
        return emptyView;
      case VIEW_STATE_ERROR:
        ensureErrorView();
        return errorView;
      case VIEW_STATE_UNKNOWN:
      default:
        return null;
    }
  }

  /**
   * Returns the current {@link ViewState}
   *
   * @return the view state
   */
  @ViewState public int getViewState() {
    return viewState;
  }

  /**
   * Sets the current {@link ViewState}
   *
   * @param state The {@link ViewState} to set {@link MultiStateLayout} to
   */
  public void setViewState(@ViewState int state) {
    if (state != viewState) {
      final int previousState = viewState;
      viewState = state;
      setView(previousState);

      if (onStateChangedListener != null) {
        onStateChangedListener.onStatedChange(viewState);
      }
    }
  }

  /**
   * Shows the {@link View} based on the {@link ViewState}
   */
  private void setView(@ViewState int previousState) {
    switch (viewState) {
      case VIEW_STATE_LOADING:
        setLoadingState(previousState);
        break;
      case VIEW_STATE_EMPTY:
        setEmptyState(previousState);
        break;
      case VIEW_STATE_ERROR:
        setErrorState(previousState);
        break;
      case VIEW_STATE_UNKNOWN:
        break;
      case VIEW_STATE_CONTENT:
      default:
        setContentState(previousState);
        break;
    }
  }

  private void ensureLoadingView() {
    if (loadingView == null && loadingViewResId > -1) {
      loadingView = LayoutInflater.from(getContext()).inflate(loadingViewResId, this, false);
      loadingView.setTag(R.id.tag_multistateview, TAG_LOADING);

      removeParent(loadingView);
      addView(loadingView, loadingView.getLayoutParams());

      if (onStateInflatedListener != null) {
        onStateInflatedListener.onStateInflated(VIEW_STATE_LOADING, loadingView);
      }

      if (viewState != VIEW_STATE_LOADING) {
        loadingView.setVisibility(GONE);
      }
    }
  }

  private void ensureEmptyView() {
    if (emptyView == null && emptyViewResId > -1) {
      emptyView = LayoutInflater.from(getContext()).inflate(emptyViewResId, this, true);
      emptyView.setTag(R.id.tag_multistateview, TAG_EMPTY);

      removeParent(emptyView);
      addView(emptyView, emptyView.getLayoutParams());

      if (onStateInflatedListener != null) {
        onStateInflatedListener.onStateInflated(VIEW_STATE_EMPTY, emptyView);
      }

      if (viewState != VIEW_STATE_EMPTY) {
        emptyView.setVisibility(GONE);
      }
    }
  }

  private void ensureErrorView() {
    if (errorView == null && errorViewResId > -1) {
      errorView = View.inflate(getContext(), errorViewResId, this);
      errorView.setTag(R.id.tag_multistateview, TAG_ERROR);

      removeParent(errorView);
      addView(errorView, errorView.getLayoutParams());

      if (onStateInflatedListener != null) {
        onStateInflatedListener.onStateInflated(VIEW_STATE_ERROR, errorView);
      }

      if (viewState != VIEW_STATE_ERROR) {
        errorView.setVisibility(GONE);
      }
    }
  }

  private void setLoadingState(@ViewState int previousState) {
    ensureLoadingView();

    Preconditions.checkNotNull(loadingView, "Loading View");

    if (contentView != null) {
      contentView.setVisibility(View.GONE);
    }
    if (errorView != null) {
      errorView.setVisibility(View.GONE);
    }
    if (emptyView != null) {
      emptyView.setVisibility(View.GONE);
    }

    if (animateViewChanges) {
      animateLayoutChange(getView(previousState));
    } else {
      loadingView.setVisibility(View.VISIBLE);
    }
  }

  private void setEmptyState(@ViewState int previousState) {
    Timber.d("setEmptyState()");
    ensureEmptyView();

    Preconditions.checkNotNull(emptyView, "Empty View");

    if (loadingView != null) {
      loadingView.setVisibility(View.GONE);
    }
    if (errorView != null) {
      errorView.setVisibility(View.GONE);
    }
    if (contentView != null) {
      contentView.setVisibility(View.GONE);
    }

    if (animateViewChanges) {
      animateLayoutChange(getView(previousState));
    } else {
      emptyView.setVisibility(View.VISIBLE);
    }
  }

  private void setErrorState(@ViewState int previousState) {
    ensureErrorView();

    Preconditions.checkNotNull(errorView, "Error View");

    if (loadingView != null) {
      loadingView.setVisibility(View.GONE);
    }
    if (contentView != null) {
      contentView.setVisibility(View.GONE);
    }
    if (emptyView != null) {
      emptyView.setVisibility(View.GONE);
    }

    if (animateViewChanges) {
      animateLayoutChange(getView(previousState));
    } else {
      errorView.setVisibility(View.VISIBLE);
    }
  }

  private void setContentState(@ViewState int previousState) {
    Preconditions.checkNotNull(contentView, "Content View");

    if (loadingView != null) {
      loadingView.setVisibility(View.GONE);
    }
    if (errorView != null) {
      errorView.setVisibility(View.GONE);
    }
    if (emptyView != null) {
      emptyView.setVisibility(View.GONE);
    }

    if (animateViewChanges) {
      animateLayoutChange(getView(previousState));
    } else {
      contentView.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Checks if the given {@link View} is valid for the Content View
   *
   * @param view The {@link View} to check
   */
  private boolean isValidContentView(View view) {
    if (contentView != null && contentView != view) {
      return false;
    }
    Object tag = view.getTag(R.id.tag_multistateview);
    if (tag == null) {
      return true;
    }
    if (tag instanceof String) {
      String viewTag = (String) tag;
      if (TextUtils.equals(viewTag, TAG_EMPTY)
          || TextUtils.equals(viewTag, TAG_ERROR)
          || TextUtils.equals(viewTag, TAG_LOADING)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Sets the view for the given view state
   *
   * @param view The {@link View} to use
   * @param state The {@link ViewState}to set
   * @param switchToState If the {@link ViewState} should be switched to
   */
  public void setViewForState(View view, @ViewState int state, boolean switchToState) {
    switch (state) {
      case VIEW_STATE_LOADING:
        if (loadingView != null) {
          removeView(loadingView);
        }
        loadingView = view;
        loadingView.setTag(R.id.tag_multistateview, TAG_LOADING);
        addView(loadingView);
        break;
      case VIEW_STATE_EMPTY:
        if (emptyView != null) {
          removeView(emptyView);
        }
        emptyView = view;
        emptyView.setTag(R.id.tag_multistateview, TAG_EMPTY);
        addView(emptyView);
        break;
      case VIEW_STATE_ERROR:
        if (errorView != null) {
          removeView(errorView);
        }
        errorView = view;
        errorView.setTag(R.id.tag_multistateview, TAG_ERROR);
        addView(errorView);
        break;
      case VIEW_STATE_CONTENT:
        if (contentView != null) {
          removeView(contentView);
        }
        contentView = view;
        if (contentView.getParent() != null) {
          ((ViewGroup) contentView.getParent()).removeView(contentView);
        }
        addView(contentView);
        break;
      case VIEW_STATE_UNKNOWN:
        break;
    }

    setView(VIEW_STATE_UNKNOWN);
    if (switchToState) {
      setViewState(state);
    }
  }

  /**
   * Sets the {@link View} for the given {@link ViewState}
   *
   * @param view The {@link View} to use
   * @param state The {@link ViewState} to set
   */
  public void setViewForState(View view, @ViewState int state) {
    setViewForState(view, state, false);
  }

  /**
   * Sets the {@link View} for the given {@link ViewState}
   *
   * @param layoutRes Layout resource id
   * @param state The {@link View} state to set
   */
  public void setViewForState(@LayoutRes int layoutRes, @ViewState int state) {
    setViewForState(layoutRes, state, false);
  }

  /**
   * Sets the {@link View} for the given {@link ViewState}
   *
   * @param layoutRes Layout resource id
   * @param state The {@link ViewState} to set
   * @param switchToState If the {@link ViewState} should be switched to
   */
  public void setViewForState(@LayoutRes int layoutRes, @ViewState int state,
      boolean switchToState) {
    final View view = View.inflate(getContext(), layoutRes, this);
    setViewForState(view, state, switchToState);
  }

  /**
   * Sets whether an animate will occur when changing between {@link ViewState}
   *
   * @param animate the animate
   */
  public void setAnimateLayoutChanges(boolean animate) {
    animateViewChanges = animate;
  }

  /**
   * Sets state listener.
   *
   * @param onStateChangedListener the on state changed listener
   * @param onStateInflatedListener the on state inflated listener
   */
  public void setStateListener(@Nullable OnStateChangedListener onStateChangedListener,
      @Nullable OnStateInflatedListener onStateInflatedListener) {
    this.onStateChangedListener = onStateChangedListener;
    this.onStateInflatedListener = onStateInflatedListener;
  }

  /**
   * Animates the layout changes between {@link ViewState}
   *
   * @param previousView The view that it was currently on
   */
  private void animateLayoutChange(@Nullable final View previousView) {
    if (previousView == null) {
      getView(viewState).setVisibility(View.VISIBLE);
      return;
    }

    previousView.setVisibility(View.VISIBLE);
    ObjectAnimator anim =
        ObjectAnimator.ofFloat(previousView, "alpha", 1.0f, 0.0f).setDuration(250L);
    anim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        previousView.setVisibility(View.GONE);
        getView(viewState).setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(getView(viewState), "alpha", 0.0f, 1.0f).setDuration(250L).start();
      }
    });
    anim.start();
  }

  private void removeParent(View view) {
    if (view.getParent() != null) {
      Timber.d("Parent: %s", view.getParent().getClass());
      ((ViewGroup) view).removeView(view);
    }
  }

  /**
   * The interface View state.
   */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({VIEW_STATE_UNKNOWN, VIEW_STATE_CONTENT, VIEW_STATE_ERROR, VIEW_STATE_EMPTY,
      VIEW_STATE_LOADING})
  public @interface ViewState {
  }

  /**
   * The interface On state changed listener.
   */
  public interface OnStateChangedListener {
    /**
     * Callback for when the {@link ViewState} has changed
     *
     * @param viewState The {@link ViewState} that was switched to
     */
    void onStatedChange(@ViewState int viewState);
  }

  /**
   * The interface On state inflated listener.
   */
  public interface OnStateInflatedListener {
    /**
     * Callback for when a {@link ViewState} has been inflated
     *
     * @param viewState The {@link ViewState} that was inflated
     * @param view The {@link View} that was inflated
     */
    void onStateInflated(@ViewState int viewState, @NonNull View view);
  }
}
