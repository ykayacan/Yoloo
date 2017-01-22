package com.yoloo.android.feature.ui.widget.badgeview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.yoloo.android.R;
import com.yoloo.android.util.DisplayUtil;

public class MaterialBadgeTextView extends AppCompatTextView {

  private static final int DEFAULT_FILL_TYPE = 0;

  private static final float SHADOW_RADIUS = 3.5f;

  private static final int FILL_SHADOW_COLOR = 0x55000000;
  private static final int KEY_SHADOW_COLOR = 0x55000000;

  private static final float X_OFFSET = 0f;
  private static final float Y_OFFSET = 1.75f;

  private int backgroundColor;
  private int borderColor;
  private float borderWidth;
  private float borderAlpha;

  private int ctType;

  private int shadowRadius;
  private int shadowYOffset;
  private int shadowXOffset;

  private int diffWH;

  private boolean isHighLightMode;

  public MaterialBadgeTextView(final Context context) {
    super(context);
    init(context, null);
  }

  public MaterialBadgeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public MaterialBadgeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    setGravity(Gravity.CENTER);

    final float density = getContext().getResources().getDisplayMetrics().density;

    shadowRadius = (int) (density * SHADOW_RADIUS);
    shadowYOffset = (int) (density * Y_OFFSET);
    shadowXOffset = (int) (density * X_OFFSET);

    final int basePadding = (shadowRadius * 2);

    final float textHeight = getTextSize();
    final float textWidth = textHeight / 4;

    diffWH = (int) (Math.abs(textHeight - textWidth) / 2);

    final int horizontalPadding = basePadding + diffWH;

    setPadding(horizontalPadding, basePadding, horizontalPadding, basePadding);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialBadgeTextView);

    backgroundColor =
        a.getColor(R.styleable.MaterialBadgeTextView_badge_backgroundColor, Color.WHITE);
    borderColor =
        a.getColor(R.styleable.MaterialBadgeTextView_badge_border_color, Color.TRANSPARENT);
    borderWidth = a.getDimension(R.styleable.MaterialBadgeTextView_badge_border_width, 0);
    borderAlpha = a.getFloat(R.styleable.MaterialBadgeTextView_badge_border_alpha, 1);
    ctType = a.getInt(R.styleable.MaterialBadgeTextView_badge_type, DEFAULT_FILL_TYPE);

    a.recycle();
  }

  @Override
  protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    super.onTextChanged(text, start, lengthBefore, lengthAfter);

    final String strText = TextUtils.isEmpty(text) ? "" : text.toString().trim();

    if (isHighLightMode && !"".equals(strText)) {
      final ViewGroup.LayoutParams lp = getLayoutParams();
      lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
      setLayoutParams(lp);
      isHighLightMode = false;
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    refreshBackgroundDrawable(w, h);
  }

  private void refreshBackgroundDrawable(int targetWidth, int targetHeight) {
    if (targetWidth <= 0 || targetHeight <= 0) {
      return;
    }

    final CharSequence text = getText();
    if (text == null) {
      return;
    }

    if (text.length() == 1) {
      final int max = Math.max(targetWidth, targetHeight);

      final int diameter = max - (2 * shadowRadius);

      final OvalShape oval = new OvalShadow(shadowRadius, diameter);
      final ShapeDrawable circle = new ShapeDrawable(oval);

      ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, circle.getPaint());
      circle.getPaint()
          .setShadowLayer(shadowRadius, shadowXOffset, shadowYOffset, KEY_SHADOW_COLOR);
      circle.getPaint().setColor(backgroundColor);
      setBackground(circle);
    } else if (text.length() > 1) {
      final SemiCircleRectDrawable sr = new SemiCircleRectDrawable();

      ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, sr.getPaint());
      sr.getPaint().setShadowLayer(shadowRadius, shadowXOffset, shadowYOffset, KEY_SHADOW_COLOR);
      sr.getPaint().setColor(backgroundColor);
      setBackground(sr);
    }
  }

  public void setBadgeCount(int count) {
    setBadgeCount(count, false);
  }

  @SuppressLint("SetTextI18n")
  public void setBadgeCount(int count, boolean goneWhenZero) {
    setVisibility(View.VISIBLE);

    if (count > 0 && count <= 99) {
      setText(String.valueOf(count));
    } else if (count > 99) {
      setText("99+");
    } else if (count <= 0) {
      setText("0");
      if (goneWhenZero) {
        setVisibility(View.GONE);
      }
    }
  }

  public void setHighLightMode() {
    setHighLightMode(false);
  }

  public void clearHighLightMode() {
    isHighLightMode = false;
    setBadgeCount(0);
  }

  public void setHighLightMode(boolean isDisplayInToolbarMenu) {
    isHighLightMode = true;
    final ViewGroup.LayoutParams params = getLayoutParams();
    params.width = DisplayUtil.dpToPx(8);
    params.height = DisplayUtil.dpToPx(8);

    if (isDisplayInToolbarMenu && params instanceof FrameLayout.LayoutParams) {
      ((FrameLayout.LayoutParams) params).topMargin = DisplayUtil.dpToPx(8);
      ((FrameLayout.LayoutParams) params).rightMargin = DisplayUtil.dpToPx(8);
    }

    setLayoutParams(params);
    final ShapeDrawable drawable = new ShapeDrawable(new OvalShape());

    ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, drawable.getPaint());
    drawable.getPaint().setColor(backgroundColor);
    drawable.getPaint().setAntiAlias(true);

    setBackground(drawable);
    setText("");
    setVisibility(View.VISIBLE);
  }

  public void setBackgroundColor(int color) {
    backgroundColor = color;
    refreshBackgroundDrawable(getWidth(), getHeight());
  }

  private class OvalShadow extends OvalShape {
    private RadialGradient radialGradient;
    private Paint shadowPaint;
    private int circleDiameter;

    OvalShadow(int shadowRadius, int circleDiameter) {
      shadowPaint = new Paint();
      MaterialBadgeTextView.this.shadowRadius = shadowRadius;
      this.circleDiameter = circleDiameter;
      radialGradient = new RadialGradient(
          this.circleDiameter / 2,
          this.circleDiameter / 2,
          MaterialBadgeTextView.this.shadowRadius,
          new int[] {FILL_SHADOW_COLOR, Color.TRANSPARENT
          },
          null,
          Shader.TileMode.CLAMP);
      shadowPaint.setShader(radialGradient);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      final int viewWidth = MaterialBadgeTextView.this.getWidth();
      final int viewHeight = MaterialBadgeTextView.this.getHeight();
      canvas.drawCircle(viewWidth / 2, viewHeight / 2, (circleDiameter / 2 + shadowRadius),
          shadowPaint);
      canvas.drawCircle(viewWidth / 2, viewHeight / 2, (circleDiameter / 2), paint);
    }
  }

  private class SemiCircleRectDrawable extends Drawable {
    private final Paint mPaint;
    private RectF rectF;

    SemiCircleRectDrawable() {
      mPaint = new Paint();
      mPaint.setAntiAlias(true);
    }

    Paint getPaint() {
      return mPaint;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
      super.setBounds(left, top, right, bottom);
      if (rectF == null) {
        rectF = new RectF(left + diffWH, top + shadowRadius + 4, right - diffWH,
            bottom - shadowRadius - 4);
      } else {
        rectF.set(left + diffWH, top + shadowRadius + 4, right - diffWH,
            bottom - shadowRadius - 4);
      }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      float R = (float) (rectF.bottom * 0.4);
      if (rectF.right < rectF.bottom) {
        R = (float) (rectF.right * 0.4);
      }
      canvas.drawRoundRect(rectF, R, R, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
      mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
      mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
      return PixelFormat.TRANSPARENT;
    }
  }
}

