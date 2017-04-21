package com.yoloo.android.ui.widget.fabmenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import com.yoloo.android.R;
import com.yoloo.android.util.AnimUtils;
import timber.log.Timber;

public class FloatingActionsMenu extends ViewGroup {
  public static final int EXPAND_UP = 0;
  public static final int EXPAND_DOWN = 1;
  public static final int EXPAND_LEFT = 2;
  public static final int EXPAND_RIGHT = 3;

  public static final int LABELS_ON_LEFT_SIDE = 0;

  private static final int ANIMATION_DURATION = 300;
  private static final float COLLAPSED_PLUS_ROTATION = 0f;
  private static final float EXPANDED_PLUS_ROTATION = 90f + 45f;

  //private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
  //private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();
  private int rootButtonSize;

  private int expandDirection;

  private ColorTransitionDrawable dimDrawable;

  // Preallocated Rect for retrieving child background padding
  private Rect childBackgroundPadding = new Rect();

  private Rect touchArea = new Rect();

  private int buttonSpacing;
  private int labelsMargin;
  private int labelsVerticalOffset;

  private boolean expanded;

  private AnimatorSet expandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
  private AnimatorSet collapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);

  private FloatingActionButton rootButton;

  private RotatingDrawable rotatingDrawable;

  private int maxButtonWidth;
  private int maxButtonHeight;

  private int labelsStyle;
  private int labelsPosition;

  private int buttonsCount;

  // View for dimming
  private View dimmingView;

  private TouchDelegateGroup touchDelegateGroup;
  private OnFloatingActionsMenuUpdateListener listener;

  public FloatingActionsMenu(Context context) {
    super(context, null);
    init(context, null);
  }

  public FloatingActionsMenu(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public FloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    buttonSpacing = getResources().getDimensionPixelSize(R.dimen.fam_spacing);
    labelsMargin = getResources().getDimensionPixelSize(R.dimen.fam_label_spacing);
    labelsVerticalOffset = 0;

    touchDelegateGroup = new TouchDelegateGroup(this);
    setTouchDelegate(touchDelegateGroup);

    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.FloatingActionsMenu, 0, 0);
    rootButtonSize =
        a.getInt(R.styleable.FloatingActionsMenu_fab_addButtonSize,
            FloatingActionButton.SIZE_NORMAL);
    final int addIconColor =
        a.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, Color.WHITE);
    expandDirection = a.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
    labelsStyle = a.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0);
    labelsPosition =
        a.getInt(R.styleable.FloatingActionsMenu_fab_labelsPosition, LABELS_ON_LEFT_SIDE);
    a.recycle();

    if (labelsStyle != 0 && expandsHorizontally()) {
      throw new IllegalStateException(
          "Action labels in horizontal expand orientation is not supported.");
    }

    // So we can catch the back button
    setFocusableInTouchMode(true);

    createRootButton(addIconColor);
  }

  public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
    this.listener = listener;
  }

  private boolean expandsHorizontally() {
    return expandDirection == EXPAND_LEFT || expandDirection == EXPAND_RIGHT;
  }

  private void createRootButton(@ColorInt int iconColor) {
    rootButton = new FloatingActionButton(getContext());

    Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_add_black_24dp);
    drawable.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);

    final RotatingDrawable rotatingDrawable = new RotatingDrawable(drawable);
    this.rotatingDrawable = rotatingDrawable;

    final ObjectAnimator collapseAnimator =
        ObjectAnimator.ofFloat(rotatingDrawable, "rotation", EXPANDED_PLUS_ROTATION,
            COLLAPSED_PLUS_ROTATION);
    final ObjectAnimator expandAnimator =
        ObjectAnimator.ofFloat(rotatingDrawable, "rotation", COLLAPSED_PLUS_ROTATION,
            EXPANDED_PLUS_ROTATION);

    collapseAnimator.setInterpolator(AnimUtils.getOvershootInterpolator());
    expandAnimator.setInterpolator(AnimUtils.getOvershootInterpolator());

    expandAnimation.play(expandAnimator);
    collapseAnimation.play(collapseAnimator);

    rootButton.setImageDrawable(rotatingDrawable);

    rootButton.setId(R.id.fab_expand_menu_button);
    rootButton.setSize(rootButtonSize);
    rootButton.setOnClickListener(v -> toggle());

    addView(rootButton, super.generateDefaultLayoutParams());
    buttonsCount++;
  }

  public void addButton(FloatingActionButton button) {
    addView(button, buttonsCount - 1);
    buttonsCount++;

    if (labelsStyle != 0) {
      createLabels();
    }
  }

  /**
   * Method to easily setup a dimming for the specified view with the specified color
   *
   * @param dimmingView the view to use for dimming (the background color will be animated)
   * @param dimmingColor the color to use for dimming (in expanded state)
   */
  public void setupWithDimmingView(View dimmingView, @ColorInt int dimmingColor) {
    this.dimmingView = dimmingView;
    dimDrawable = new ColorTransitionDrawable(Color.TRANSPARENT, dimmingColor);
    ViewCompat.setBackground(dimmingView, dimDrawable);
    // apply the appbar elevation so the dim gets rendered over it
    ViewCompat.setElevation(this,
        getContext().getResources().getDimensionPixelSize(R.dimen.design_fab_elevation));
    ViewCompat.setElevation(dimmingView,
        getContext().getResources().getDimensionPixelSize(R.dimen.dim_elevation));
    // set click listener and disable clicks
    dimmingView.setOnClickListener(v -> collapse());
    dimmingView.setClickable(false);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    measureChildren(widthMeasureSpec, heightMeasureSpec);

    int width = 0;
    int height = 0;

    maxButtonWidth = 0;
    maxButtonHeight = 0;
    int maxLabelWidth = 0;

    for (int i = 0; i < buttonsCount; i++) {
      final View child = getChildAt(i);

      if (child.getVisibility() == GONE) {
        continue;
      }

      switch (expandDirection) {
        case EXPAND_UP:
        case EXPAND_DOWN:
          maxButtonWidth = Math.max(maxButtonWidth, child.getMeasuredWidth());
          height += child.getMeasuredHeight();
          break;
        case EXPAND_LEFT:
        case EXPAND_RIGHT:
          width += child.getMeasuredWidth();
          maxButtonHeight = Math.max(maxButtonHeight, child.getMeasuredHeight());
          break;
      }

      // Consider background padding in size measurement to account for compatibility shadow
      child.getBackground().getPadding(childBackgroundPadding);

      if (!expandsHorizontally()) {
        maxButtonWidth = Math.max(maxButtonWidth,
            child.getMeasuredWidth() - childBackgroundPadding.left - childBackgroundPadding.right);
        height +=
            child.getMeasuredHeight() - childBackgroundPadding.top - childBackgroundPadding.bottom;
        LabelView label = (LabelView) child.getTag(R.id.fab_label);
        //TextView label = (TextView) child.getTag(R.id.fab_label);
        if (label != null) {
          maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
        }
      } else {
        width +=
            child.getMeasuredWidth() - childBackgroundPadding.left - childBackgroundPadding.right;
        maxButtonHeight = Math.max(maxButtonHeight,
            child.getMeasuredHeight() - childBackgroundPadding.top - childBackgroundPadding.bottom);
      }
    }

    LayoutParams rootButtonParams = (LayoutParams) rootButton.getLayoutParams();
    if (!expandsHorizontally()) {
      width = maxButtonWidth + (maxLabelWidth > 0 ? maxLabelWidth + labelsMargin : 0);
      width += rootButtonParams.leftMargin + rootButtonParams.rightMargin;
      height += buttonSpacing * (buttonsCount - 1);
      height = adjustForOvershoot(height);
      height += (expandDirection == EXPAND_UP) ? rootButtonParams.bottomMargin
          + childBackgroundPadding.top
          : rootButtonParams.topMargin + childBackgroundPadding.bottom;
    } else {
      height = maxButtonHeight;
      height += rootButtonParams.topMargin + rootButtonParams.rightMargin;
      width += buttonSpacing * (buttonsCount - 1);
      width = adjustForOvershoot(width);
      width += (expandDirection == EXPAND_LEFT) ? rootButtonParams.rightMargin
          + childBackgroundPadding.left
          : rootButtonParams.leftMargin + childBackgroundPadding.right;
    }

    switch (expandDirection) {
      case EXPAND_UP:
      case EXPAND_DOWN:
        height += buttonSpacing * (buttonsCount - 1);
        height = adjustForOvershoot(height);
        break;
      case EXPAND_LEFT:
      case EXPAND_RIGHT:
        width += buttonSpacing * (buttonsCount - 1);
        width = adjustForOvershoot(width);
        break;
    }

    setMeasuredDimension(width, height);
  }

  private int adjustForOvershoot(int dimension) {
    return dimension * 12 / 10;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    switch (expandDirection) {
      case EXPAND_UP:
      case EXPAND_DOWN:
        boolean expandUp = expandDirection == EXPAND_UP;

        if (changed) {
          touchDelegateGroup.clearTouchDelegates();
        }

        // Consider margin and background padding to account for compatibility shadow
        rootButton.getBackground().getPadding(childBackgroundPadding);
        LayoutParams mainButtonParamsHorizontal = (LayoutParams) rootButton.getLayoutParams();

        int addButtonY = expandUp ? b - t - rootButton.getMeasuredHeight()
            + childBackgroundPadding.top
            + childBackgroundPadding.bottom - mainButtonParamsHorizontal.bottomMargin
            : mainButtonParamsHorizontal.topMargin;
        // Ensure fabButton is centered on the line where the buttons should be
        int buttonsHorizontalCenter = labelsPosition == LABELS_ON_LEFT_SIDE
            ? r - l - maxButtonWidth / 2 - mainButtonParamsHorizontal.rightMargin
            : maxButtonWidth / 2 + mainButtonParamsHorizontal.leftMargin;
        int addButtonLeft = buttonsHorizontalCenter
            - (rootButton.getMeasuredWidth()
            - childBackgroundPadding.left
            - childBackgroundPadding.right) / 2;
        rootButton.layout(addButtonLeft - childBackgroundPadding.left,
            addButtonY - childBackgroundPadding.top,
            addButtonLeft - childBackgroundPadding.left + rootButton.getMeasuredWidth(),
            addButtonY - childBackgroundPadding.top + rootButton.getMeasuredHeight());
        addButtonY -= childBackgroundPadding.top;

        int labelsOffset = maxButtonWidth / 2 + labelsMargin;
        int labelsXNearButton = labelsPosition == LABELS_ON_LEFT_SIDE
            ? buttonsHorizontalCenter - labelsOffset
            : buttonsHorizontalCenter + labelsOffset;

        int nextY = expandUp ?
            addButtonY + childBackgroundPadding.top - buttonSpacing :
            addButtonY + rootButton.getMeasuredHeight()
                - childBackgroundPadding.top
                - childBackgroundPadding.bottom + buttonSpacing;

        for (int i = buttonsCount - 1; i >= 0; i--) {
          final View child = getChildAt(i);

          if (child == rootButton || child.getVisibility() == GONE) continue;

          // Consider background padding to account for compatibility shadow
          child.getBackground().getPadding(childBackgroundPadding);
          int childX = buttonsHorizontalCenter
              - (child.getMeasuredWidth()
              - childBackgroundPadding.left
              - childBackgroundPadding.right) / 2;
          int childY = expandUp ? nextY - child.getMeasuredHeight()
              + childBackgroundPadding.top
              + childBackgroundPadding.bottom : nextY;
          child.layout(childX - childBackgroundPadding.left, childY - childBackgroundPadding.top,
              childX - childBackgroundPadding.left + child.getMeasuredWidth(),
              childY - childBackgroundPadding.top + child.getMeasuredHeight());
          childY -= childBackgroundPadding.top;

          float collapsedTranslation = addButtonY - childY;
          float expandedTranslation = 0f;

          child.setTranslationY(expanded ? expandedTranslation : collapsedTranslation);
          child.setAlpha(expanded ? 1f : 0f);

          LayoutParams params = (LayoutParams) child.getLayoutParams();
          params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
          params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
          params.setAnimationsTarget(child);

          LabelView label = (LabelView) child.getTag(R.id.fab_label);
          //final View label = (View) child.getTag(R.id.fab_label);
          if (label != null) {
            int labelXAwayFromButton = labelsPosition == LABELS_ON_LEFT_SIDE
                ? labelsXNearButton - label.getMeasuredWidth()
                : labelsXNearButton + label.getMeasuredWidth();

            int labelLeft = labelsPosition == LABELS_ON_LEFT_SIDE
                ? labelXAwayFromButton
                : labelsXNearButton;

            int labelRight = labelsPosition == LABELS_ON_LEFT_SIDE
                ? labelsXNearButton
                : labelXAwayFromButton;

            int labelTop = childY - labelsVerticalOffset
                + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

            label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());

            touchArea.left = Math.min(childX, labelLeft);
            touchArea.top =  childY - buttonSpacing / 2;
            touchArea.right = Math.max(childX + child.getMeasuredWidth(), labelRight);
            touchArea.bottom = childY + child.getMeasuredHeight() + buttonSpacing / 2;
            /*Rect touchArea = new Rect(
                Math.min(childX, labelLeft),
                childY - buttonSpacing / 2,
                Math.max(childX + child.getMeasuredWidth(), labelRight),
                childY + child.getMeasuredHeight() + buttonSpacing / 2);*/
            touchDelegateGroup.addTouchDelegate(new TouchDelegate(touchArea, child));

            label.setTranslationY(expanded ? expandedTranslation : collapsedTranslation);
            label.setAlpha(expanded ? 1f : 0f);

            LayoutParams labelParams = (LayoutParams) label.getLayoutParams();
            labelParams.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
            labelParams.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
            labelParams.setAnimationsTarget(label);
          }

          nextY = expandUp ?
              childY + childBackgroundPadding.top - buttonSpacing :
              childY + child.getMeasuredHeight()
                  - childBackgroundPadding.top
                  - childBackgroundPadding.right + buttonSpacing;
        }
        break;

      case EXPAND_LEFT:
      case EXPAND_RIGHT:
        boolean expandLeft = expandDirection == EXPAND_LEFT;

        int addButtonX = expandLeft ? r - l - rootButton.getMeasuredWidth() : 0;
        // Ensure rootButton is centered on the line where the buttons should be
        int addButtonTop =
            b - t - maxButtonHeight + (maxButtonHeight - rootButton.getMeasuredHeight()) / 2;
        rootButton.layout(addButtonX, addButtonTop, addButtonX + rootButton.getMeasuredWidth(),
            addButtonTop + rootButton.getMeasuredHeight());

        int nextX = expandLeft ?
            addButtonX - buttonSpacing :
            addButtonX + rootButton.getMeasuredWidth() + buttonSpacing;

        for (int i = buttonsCount - 1; i >= 0; i--) {
          final View child = getChildAt(i);

          if (child == rootButton || child.getVisibility() == GONE) continue;

          int childX = expandLeft ? nextX - child.getMeasuredWidth() : nextX;
          int childY =
              addButtonTop + (rootButton.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
          child.layout(childX, childY, childX + child.getMeasuredWidth(),
              childY + child.getMeasuredHeight());

          float collapsedTranslation = addButtonX - childX;
          float expandedTranslation = 0f;

          child.setTranslationX(expanded ? expandedTranslation : collapsedTranslation);
          child.setAlpha(expanded ? 1f : 0f);

          LayoutParams params = (LayoutParams) child.getLayoutParams();
          params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
          params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
          params.setAnimationsTarget(child);

          nextX = expandLeft ?
              childX - buttonSpacing :
              childX + child.getMeasuredWidth() + buttonSpacing;
        }

        break;
    }
  }

  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(super.generateDefaultLayoutParams(), expandDirection,
        collapseAnimation, expandAnimation);
  }

  @Override
  public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(super.generateLayoutParams(attrs), expandDirection, collapseAnimation,
        expandAnimation);
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(super.generateLayoutParams(p), expandDirection, collapseAnimation,
        expandAnimation);
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    bringChildToFront(rootButton);
    buttonsCount = getChildCount();

    if (labelsStyle != 0) {
      createLabels();
    }
  }

  private void createLabels() {
    Context context = new ContextThemeWrapper(getContext(), labelsStyle);

    for (int i = 0; i < buttonsCount; i++) {
      FloatingActionButton button = (FloatingActionButton) getChildAt(i);
      CharSequence title = button.getContentDescription();

      if (button == rootButton || title == null ||
          button.getTag(R.id.fab_label) != null) {
        continue;
      }

      final LabelView label = new LabelView(context);
      label.setAnimationOffset(maxButtonWidth / 2f + labelsMargin);
      label.setTextAppearance(getContext(), labelsStyle);
      label.setText(title);
      addView(label);

      button.setTag(R.id.fab_label, label);
    }
  }

  public void collapse() {
    collapse(false);
  }

  public void collapseImmediately() {
    collapse(true);
  }

  private void collapse(boolean immediately) {
    if (expanded) {
      expanded = false;

      if (dimDrawable != null) {
        dimDrawable.reverseTransition(immediately ? 0 : ANIMATION_DURATION);
      }

      touchDelegateGroup.setEnabled(false);
      collapseAnimation.setDuration(immediately ? 0 : ANIMATION_DURATION);
      collapseAnimation.start();
      expandAnimation.cancel();

      if (listener != null) {
        listener.onMenuCollapsed();
      }

      // So we don't catch the back button anymore
      clearFocus();
      if (dimmingView != null) {
        dimmingView.setClickable(false);
      }
    }
  }

  public void toggle() {
    if (expanded) {
      collapse();
    } else {
      expand();
    }
  }

  public void expand() {
    if (!expanded) {
      expanded = true;

      if (dimDrawable != null) {
        dimDrawable.startTransition(ANIMATION_DURATION);
      }

      touchDelegateGroup.setEnabled(true);
      collapseAnimation.cancel();
      expandAnimation.start();

      if (listener != null) {
        listener.onMenuExpanded();
      }

      // So we can catch the back button
      requestFocus();
      if (dimmingView != null) {
        Timber.d("Dim");
        dimmingView.setClickable(true);
      }
    }
  }

  public boolean isExpanded() {
    return expanded;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    rootButton.setEnabled(enabled);
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.expanded = expanded;

    return savedState;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      SavedState savedState = (SavedState) state;
      expanded = savedState.expanded;
      touchDelegateGroup.setEnabled(expanded);

      if (rotatingDrawable != null) {
        rotatingDrawable.setRotation(expanded ? EXPANDED_PLUS_ROTATION : COLLAPSED_PLUS_ROTATION);
      }

      super.onRestoreInstanceState(savedState.getSuperState());
    } else {
      super.onRestoreInstanceState(state);
    }
  }

  public interface OnFloatingActionsMenuUpdateListener {
    void onMenuExpanded();

    void onMenuCollapsed();
  }

  private static class RotatingDrawable extends LayerDrawable {
    private float rotation;

    RotatingDrawable(Drawable drawable) {
      super(new Drawable[] {drawable});
    }

    public float getRotation() {
      return rotation;
    }

    @Keep void setRotation(float rotation) {
      this.rotation = rotation;
      invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
      canvas.save();
      canvas.rotate(rotation, getBounds().centerX(), getBounds().centerY());
      super.draw(canvas);
      canvas.restore();
    }
  }

  private static class SavedState extends BaseSavedState {
    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

      @Override
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
    boolean expanded;

    SavedState(Parcelable parcel) {
      super(parcel);
    }

    private SavedState(Parcel in) {
      super(in);
      expanded = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(expanded ? 1 : 0);
    }
  }

  private static class LayoutParams extends MarginLayoutParams {

    private final AnimatorSet collapseAnimation;
    private final AnimatorSet expandAnimation;
    private ObjectAnimator mExpandDir = new ObjectAnimator();
    private ObjectAnimator mExpandAlpha = new ObjectAnimator();
    private ObjectAnimator mCollapseDir = new ObjectAnimator();
    private ObjectAnimator mCollapseAlpha = new ObjectAnimator();
    private boolean animationsSetToPlay;

    LayoutParams(ViewGroup.LayoutParams source, int expandDirection, AnimatorSet collapseAnimation,
        AnimatorSet expandAnimation) {
      super(source);
      this.collapseAnimation = collapseAnimation;
      this.expandAnimation = expandAnimation;

      mExpandDir.setInterpolator(AnimUtils.getOvershootInterpolator());
      mExpandAlpha.setInterpolator(AnimUtils.getDecelerateInterpolator());
      mCollapseDir.setInterpolator(AnimUtils.getDecelerateInterpolator());
      mCollapseAlpha.setInterpolator(AnimUtils.getDecelerateInterpolator());

      mCollapseAlpha.setProperty(View.ALPHA);
      mCollapseAlpha.setFloatValues(1f, 0f);

      mExpandAlpha.setProperty(View.ALPHA);
      mExpandAlpha.setFloatValues(0f, 1f);

      switch (expandDirection) {
        case EXPAND_UP:
        case EXPAND_DOWN:
          mCollapseDir.setProperty(View.TRANSLATION_Y);
          mExpandDir.setProperty(View.TRANSLATION_Y);
          break;
        case EXPAND_LEFT:
        case EXPAND_RIGHT:
          mCollapseDir.setProperty(View.TRANSLATION_X);
          mExpandDir.setProperty(View.TRANSLATION_X);
          break;
      }
    }

    void setAnimationsTarget(View view) {
      mCollapseAlpha.setTarget(view);
      mCollapseDir.setTarget(view);
      mExpandAlpha.setTarget(view);
      mExpandDir.setTarget(view);

      // Now that the animations have targets, set them to be played
      if (!animationsSetToPlay) {
        addLayerTypeListener(mExpandDir, view);
        addLayerTypeListener(mCollapseDir, view);

        collapseAnimation.play(mCollapseAlpha);
        collapseAnimation.play(mCollapseDir);
        expandAnimation.play(mExpandAlpha);
        expandAnimation.play(mExpandDir);
        animationsSetToPlay = true;
      }
    }

    private void addLayerTypeListener(Animator animator, final View view) {
      animator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          view.setLayerType(LAYER_TYPE_NONE, null);
        }

        @Override
        public void onAnimationStart(Animator animation) {
          view.setLayerType(LAYER_TYPE_HARDWARE, null);
        }
      });
    }
  }
}
