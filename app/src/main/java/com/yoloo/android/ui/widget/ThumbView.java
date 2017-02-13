package com.yoloo.android.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yoloo.android.R;
import com.yoloo.android.util.Preconditions;

public class ThumbView extends FrameLayout {

  @BindView(R.id.image_thumb_preview)
  ImageView ivThumbPreview;

  private OnDismissListener listener;

  public ThumbView(Context context) {
    super(context);
    init();
  }

  public ThumbView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    inflate(getContext(), R.layout.merge_view_thumb, this);
    ButterKnife.bind(this);
  }

  public void setThumbPreview(Uri uri) {
    ivThumbPreview.setImageURI(uri);
  }

  public void setImageDrawable(Drawable drawable) {
    ivThumbPreview.setImageDrawable(drawable);
  }

  public void setListener(OnDismissListener listener) {
    this.listener = listener;
  }

  @OnClick(R.id.image_btn_cancel)
  void dismiss() {
    Preconditions.checkNotNull(listener, "OnDismissListener can not be empty!");
    listener.onDismiss(this);
  }

  public interface OnDismissListener {
    void onDismiss(View view);
  }
}
