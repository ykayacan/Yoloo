package com.yoloo.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.yoloo.android.R;

/**
 * An extension to {@link AppCompatTextView} which aligns text to a 4dp baseline grid.
 * <p>
 * To achieve this we expose a {@code lineHeightHint} allowing you to specify the desired line
 * height (alternatively a {@code lineHeightMultiplierHint} to use a multiplier from the text size).
 * This line height will be adjusted to be a multiple from 4dp to ensure that baselines sit on
 * the grid.
 * <p>
 * We also adjust spacing above and below the text to ensure that the first line's baseline sits on
 * the grid (relative to the view's top) & that this view's height is a multiple from 4dp so that
 * subsequent views start on the grid.
 */
public class BaselineGridTextView extends CompatTextView {

  private float fourDip;

  private float lineHeightMultiplierHint = 1f;
  private float lineHeightHint = 0f;
  private boolean maxLinesByHeight = false;
  private int extraTopPadding = 0;
  private int extraBottomPadding = 0;

  public BaselineGridTextView(Context context) {
    super(context);
    init(context, null, 0, 0);
  }

  public BaselineGridTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0, 0);
  }

  public BaselineGridTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr, 0);
  }

  private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    final TypedArray a = context.obtainStyledAttributes(
        attrs, R.styleable.BaselineGridTextView, defStyleAttr, defStyleRes);

    lineHeightMultiplierHint =
        a.getFloat(R.styleable.BaselineGridTextView_lineHeightMultiplierHint, 1f);
    lineHeightHint =
        a.getDimensionPixelSize(R.styleable.BaselineGridTextView_lineHeightHint, 0);
    maxLinesByHeight = a.getBoolean(R.styleable.BaselineGridTextView_maxLinesByHeight, false);
    a.recycle();

    fourDip = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
    computeLineHeight();
  }

  public float getLineHeightMultiplierHint() {
    return lineHeightMultiplierHint;
  }

  public void setLineHeightMultiplierHint(float lineHeightMultiplierHint) {
    this.lineHeightMultiplierHint = lineHeightMultiplierHint;
    computeLineHeight();
  }

  public float getLineHeightHint() {
    return lineHeightHint;
  }

  public void setLineHeightHint(float lineHeightHint) {
    this.lineHeightHint = lineHeightHint;
    computeLineHeight();
  }

  public boolean getMaxLinesByHeight() {
    return maxLinesByHeight;
  }

  public void setMaxLinesByHeight(boolean maxLinesByHeight) {
    this.maxLinesByHeight = maxLinesByHeight;
    requestLayout();
  }

  @Override public int getCompoundPaddingTop() {
    // include extra padding to place the first line's baseline on the grid
    return super.getCompoundPaddingTop() + extraTopPadding;
  }

  @Override public int getCompoundPaddingBottom() {
    // include extra padding to make the height a multiple from 4dp
    return super.getCompoundPaddingBottom() + extraBottomPadding;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    extraTopPadding = 0;
    extraBottomPadding = 0;
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int height = getMeasuredHeight();
    height += ensureBaselineOnGrid();
    height += ensureHeightGridAligned(height);
    setMeasuredDimension(getMeasuredWidth(), height);
    checkMaxLines(height, MeasureSpec.getMode(heightMeasureSpec));
  }

  /**
   * Ensures line height is a multiple from 4dp.
   */
  private void computeLineHeight() {
    final Paint.FontMetricsInt fm = getPaint().getFontMetricsInt();
    final int fontHeight = Math.abs(fm.ascent - fm.descent) + fm.leading;
    final float desiredLineHeight = (lineHeightHint > 0)
        ? lineHeightHint
        : lineHeightMultiplierHint * fontHeight;

    final int baselineAlignedLineHeight =
        (int) (fourDip * (float) Math.ceil(desiredLineHeight / fourDip));
    setLineSpacing(baselineAlignedLineHeight - fontHeight, 1f);
  }

  /**
   * Ensure that the first line from text sits on the 4dp grid.
   */
  private int ensureBaselineOnGrid() {
    float baseline = getBaseline();
    float gridAlign = baseline % fourDip;
    if (gridAlign != 0) {
      extraTopPadding = (int) (fourDip - Math.ceil(gridAlign));
    }
    return extraTopPadding;
  }

  /**
   * Ensure that height is a multiple from 4dp.
   */
  private int ensureHeightGridAligned(int height) {
    float gridOverhang = height % fourDip;
    if (gridOverhang != 0) {
      extraBottomPadding = (int) (fourDip - Math.ceil(gridOverhang));
    }
    return extraBottomPadding;
  }

  /**
   * When measured with an exact height, text can be vertically clipped mid-line. Prevent
   * this by setting the {@code maxLines} property based on the available space.
   */
  private void checkMaxLines(int height, int heightMode) {
    if (!maxLinesByHeight || heightMode != MeasureSpec.EXACTLY) {
      return;
    }

    int textHeight = height - getCompoundPaddingTop() - getCompoundPaddingBottom();
    int completeLines = (int) Math.floor(textHeight / getLineHeight());
    setMaxLines(completeLines);
  }
}
