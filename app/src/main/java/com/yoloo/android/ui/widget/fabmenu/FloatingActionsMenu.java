package com.yoloo.android.ui.widget.fabmenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.yoloo.android.R;
import com.yoloo.android.util.AnimUtils;
import com.yoloo.android.util.VersionUtil;

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
  private int mAddButtonSize;
  private int expandDirection;
  private int mButtonSpacing;
  private int mLabelsMargin;
  private int mLabelsVerticalOffset;
  private boolean mExpanded;
  private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
  private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
  private FloatingActionButton addButton;
  private RotatingDrawable mRotatingDrawable;
  private int mMaxButtonWidth;
  private int mMaxButtonHeight;
  private int mLabelsStyle;
  private int mLabelsPosition;
  private int mButtonsCount;
  private TouchDelegateGroup mTouchDelegateGroup;
  private OnFloatingActionsMenuUpdateListener mListener;

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
    mButtonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing)
        - getResources().getDimension(R.dimen.fab_shadow_radius)
        - getResources().getDimension(R.dimen.fab_shadow_offset));
    mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.fab_labels_margin);
    mLabelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);

    mTouchDelegateGroup = new TouchDelegateGroup(this);
    setTouchDelegate(mTouchDelegateGroup);

    /*TypedArray a =
        context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
    mAddButtonSize = a.getInt(R.styleable.FloatingActionsMenu_fab_addButtonSize,
        FloatingActionButton.SIZE_NORMAL);
    expandDirection = a.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
    mLabelsStyle = a.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0);
    mLabelsPosition =
        a.getInt(R.styleable.FloatingActionsMenu_fab_labelsPosition, LABELS_ON_LEFT_SIDE);
    a.recycle();*/

    if (mLabelsStyle != 0 && expandsHorizontally()) {
      throw new IllegalStateException(
          "Action labels in horizontal expand orientation is not supported.");
    }

    createAddButton(context);
  }

  public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
    mListener = listener;
  }

  private boolean expandsHorizontally() {
    return expandDirection == EXPAND_LEFT || expandDirection == EXPAND_RIGHT;
  }

  private void createAddButton(Context context) {
    addButton = new FloatingActionButton(getContext());

    Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_add_black_24dp);

    final RotatingDrawable rotatingDrawable = new RotatingDrawable(drawable);
    mRotatingDrawable = rotatingDrawable;

    final ObjectAnimator collapseAnimator =
        ObjectAnimator.ofFloat(rotatingDrawable, "rotation", EXPANDED_PLUS_ROTATION,
            COLLAPSED_PLUS_ROTATION);
    final ObjectAnimator expandAnimator =
        ObjectAnimator.ofFloat(rotatingDrawable, "rotation", COLLAPSED_PLUS_ROTATION,
            EXPANDED_PLUS_ROTATION);

    collapseAnimator.setInterpolator(AnimUtils.getOvershootInterpolator());
    expandAnimator.setInterpolator(AnimUtils.getOvershootInterpolator());

    mExpandAnimation.play(expandAnimator);
    mCollapseAnimation.play(collapseAnimator);

    addButton.setImageDrawable(rotatingDrawable);

    addButton.setId(R.id.fab_expand_menu_button);
    addButton.setSize(mAddButtonSize);
    addButton.setOnClickListener(v -> toggle());

    addView(addButton, super.generateDefaultLayoutParams());
    mButtonsCount++;
  }

  public void addButton(FloatingActionButton button) {
    addView(button, mButtonsCount - 1);
    mButtonsCount++;

    if (mLabelsStyle != 0) {
      createLabels();
    }
  }

  private int getColor(@ColorRes int id) {
    return ContextCompat.getColor(getContext(), id);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    measureChildren(widthMeasureSpec, heightMeasureSpec);

    int width = 0;
    int height = 0;

    mMaxButtonWidth = 0;
    mMaxButtonHeight = 0;
    int maxLabelWidth = 0;

    for (int i = 0; i < mButtonsCount; i++) {
      View child = getChildAt(i);

      if (child.getVisibility() == GONE) {
        continue;
      }

      switch (expandDirection) {
        case EXPAND_UP:
        case EXPAND_DOWN:
          mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
          height += child.getMeasuredHeight();
          break;
        case EXPAND_LEFT:
        case EXPAND_RIGHT:
          width += child.getMeasuredWidth();
          mMaxButtonHeight = Math.max(mMaxButtonHeight, child.getMeasuredHeight());
          break;
      }

      if (!expandsHorizontally()) {
        TextView label = (TextView) child.getTag(R.id.fab_label);
        if (label != null) {
          maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
        }
      }
    }

    if (!expandsHorizontally()) {
      width = mMaxButtonWidth + (maxLabelWidth > 0 ? maxLabelWidth + mLabelsMargin : 0);
    } else {
      height = mMaxButtonHeight;
    }

    switch (expandDirection) {
      case EXPAND_UP:
      case EXPAND_DOWN:
        height += mButtonSpacing * (mButtonsCount - 1);
        height = adjustForOvershoot(height);
        break;
      case EXPAND_LEFT:
      case EXPAND_RIGHT:
        width += mButtonSpacing * (mButtonsCount - 1);
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
          mTouchDelegateGroup.clearTouchDelegates();
        }

        int addButtonY = expandUp ? b - t - addButton.getMeasuredHeight() : 0;
        // Ensure addButton is centered on the line where the buttons should be
        int buttonsHorizontalCenter = mLabelsPosition == LABELS_ON_LEFT_SIDE
            ? r - l - mMaxButtonWidth / 2
            : mMaxButtonWidth / 2;
        int addButtonLeft = buttonsHorizontalCenter - addButton.getMeasuredWidth() / 2;
        addButton.layout(addButtonLeft, addButtonY, addButtonLeft + addButton.getMeasuredWidth(),
            addButtonY + addButton.getMeasuredHeight());

        int labelsOffset = mMaxButtonWidth / 2 + mLabelsMargin;
        int labelsXNearButton = mLabelsPosition == LABELS_ON_LEFT_SIDE
            ? buttonsHorizontalCenter - labelsOffset
            : buttonsHorizontalCenter + labelsOffset;

        int nextY = expandUp ?
            addButtonY - mButtonSpacing :
            addButtonY + addButton.getMeasuredHeight() + mButtonSpacing;

        for (int i = mButtonsCount - 1; i >= 0; i--) {
          final View child = getChildAt(i);

          if (child == addButton || child.getVisibility() == GONE) continue;

          int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
          int childY = expandUp ? nextY - child.getMeasuredHeight() : nextY;
          child.layout(childX, childY, childX + child.getMeasuredWidth(),
              childY + child.getMeasuredHeight());

          float collapsedTranslation = addButtonY - childY;
          float expandedTranslation = 0f;

          child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
          child.setAlpha(mExpanded ? 1f : 0f);

          LayoutParams params = (LayoutParams) child.getLayoutParams();
          params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
          params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
          params.setAnimationsTarget(child);

          View label = (View) child.getTag(R.id.fab_label);
          if (label != null) {
            int labelXAwayFromButton = mLabelsPosition == LABELS_ON_LEFT_SIDE
                ? labelsXNearButton - label.getMeasuredWidth()
                : labelsXNearButton + label.getMeasuredWidth();

            int labelLeft = mLabelsPosition == LABELS_ON_LEFT_SIDE
                ? labelXAwayFromButton
                : labelsXNearButton;

            int labelRight = mLabelsPosition == LABELS_ON_LEFT_SIDE
                ? labelsXNearButton
                : labelXAwayFromButton;

            int labelTop = childY - mLabelsVerticalOffset
                + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

            label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());

            Rect touchArea = new Rect(
                Math.min(childX, labelLeft),
                childY - mButtonSpacing / 2,
                Math.max(childX + child.getMeasuredWidth(), labelRight),
                childY + child.getMeasuredHeight() + mButtonSpacing / 2);
            mTouchDelegateGroup.addTouchDelegate(new TouchDelegate(touchArea, child));

            label.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
            label.setAlpha(mExpanded ? 1f : 0f);

            LayoutParams labelParams = (LayoutParams) label.getLayoutParams();
            labelParams.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
            labelParams.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
            labelParams.setAnimationsTarget(label);
          }

          nextY = expandUp ?
              childY - mButtonSpacing :
              childY + child.getMeasuredHeight() + mButtonSpacing;
        }
        break;

      case EXPAND_LEFT:
      case EXPAND_RIGHT:
        boolean expandLeft = expandDirection == EXPAND_LEFT;

        int addButtonX = expandLeft ? r - l - addButton.getMeasuredWidth() : 0;
        // Ensure addButton is centered on the line where the buttons should be
        int addButtonTop =
            b - t - mMaxButtonHeight + (mMaxButtonHeight - addButton.getMeasuredHeight()) / 2;
        addButton.layout(addButtonX, addButtonTop, addButtonX + addButton.getMeasuredWidth(),
            addButtonTop + addButton.getMeasuredHeight());

        int nextX = expandLeft ?
            addButtonX - mButtonSpacing :
            addButtonX + addButton.getMeasuredWidth() + mButtonSpacing;

        for (int i = mButtonsCount - 1; i >= 0; i--) {
          final View child = getChildAt(i);

          if (child == addButton || child.getVisibility() == GONE) continue;

          int childX = expandLeft ? nextX - child.getMeasuredWidth() : nextX;
          int childY =
              addButtonTop + (addButton.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
          child.layout(childX, childY, childX + child.getMeasuredWidth(),
              childY + child.getMeasuredHeight());

          float collapsedTranslation = addButtonX - childX;
          float expandedTranslation = 0f;

          child.setTranslationX(mExpanded ? expandedTranslation : collapsedTranslation);
          child.setAlpha(mExpanded ? 1f : 0f);

          LayoutParams params = (LayoutParams) child.getLayoutParams();
          params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
          params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
          params.setAnimationsTarget(child);

          nextX = expandLeft ?
              childX - mButtonSpacing :
              childX + child.getMeasuredWidth() + mButtonSpacing;
        }

        break;
    }
  }

  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(super.generateDefaultLayoutParams(), expandDirection,
        mCollapseAnimation, mExpandAnimation);
  }

  @Override
  public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(super.generateLayoutParams(attrs), expandDirection, mCollapseAnimation,
        mExpandAnimation);
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(super.generateLayoutParams(p), expandDirection, mCollapseAnimation,
        mExpandAnimation);
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return super.checkLayoutParams(p);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    bringChildToFront(addButton);
    mButtonsCount = getChildCount();

    if (mLabelsStyle != 0) {
      createLabels();
    }
  }

  private void createLabels() {
    Context context = new ContextThemeWrapper(getContext(), mLabelsStyle);

    for (int i = 0; i < mButtonsCount; i++) {
      FloatingActionButton button = (FloatingActionButton) getChildAt(i);
      CharSequence title = button.getContentDescription();

      if (button == addButton || title == null ||
          button.getTag(R.id.fab_label) != null) {
        continue;
      }

      TextView label = new TextView(context);
      if (VersionUtil.hasM()) {
        label.setTextAppearance(mLabelsStyle);
      } else {
        label.setTextAppearance(getContext(), mLabelsStyle);
      }
      label.setText(button.getContentDescription());
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
    if (mExpanded) {
      mExpanded = false;
      mTouchDelegateGroup.setEnabled(false);
      mCollapseAnimation.setDuration(immediately ? 0 : ANIMATION_DURATION);
      mCollapseAnimation.start();
      mExpandAnimation.cancel();

      if (mListener != null) {
        mListener.onMenuCollapsed();
      }
    }
  }

  public void toggle() {
    if (mExpanded) {
      collapse();
    } else {
      expand();
    }
  }

  public void expand() {
    if (!mExpanded) {
      mExpanded = true;
      mTouchDelegateGroup.setEnabled(true);
      mCollapseAnimation.cancel();
      mExpandAnimation.start();

      if (mListener != null) {
        mListener.onMenuExpanded();
      }
    }
  }

  public boolean isExpanded() {
    return mExpanded;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    addButton.setEnabled(enabled);
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.mExpanded = mExpanded;

    return savedState;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      SavedState savedState = (SavedState) state;
      mExpanded = savedState.mExpanded;
      mTouchDelegateGroup.setEnabled(mExpanded);

      if (mRotatingDrawable != null) {
        mRotatingDrawable.setRotation(mExpanded ? EXPANDED_PLUS_ROTATION : COLLAPSED_PLUS_ROTATION);
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
    boolean mExpanded;

    SavedState(Parcelable parcel) {
      super(parcel);
    }

    private SavedState(Parcel in) {
      super(in);
      mExpanded = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(mExpanded ? 1 : 0);
    }
  }

  private static class LayoutParams extends ViewGroup.LayoutParams {

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
