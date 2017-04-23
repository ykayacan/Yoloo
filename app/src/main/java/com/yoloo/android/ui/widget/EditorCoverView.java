package com.yoloo.android.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yoloo.android.R;

public class EditorCoverView extends FrameLayout {

  @BindView(R.id.iv_slide) ImageView ivCover;
  @BindView(R.id.tv_add_cover) TextView tvAddCoverText;

  @BindString(R.string.label_click_add_image) String clickToAddString;
  @BindString(R.string.label_click_remove_image) String clickToRemoveString;

  private OnAddImageListener onAddImageListener;

  public EditorCoverView(Context context) {
    super(context);
    init();
  }

  public EditorCoverView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public EditorCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    View view = inflate(getContext(), R.layout.view_editor_cover_image, this);
    ButterKnife.bind(this, view);
    tvAddCoverText.setText(clickToAddString);
  }

  public void setOnAddImageListener(OnAddImageListener onAddImageListener) {
    this.onAddImageListener = onAddImageListener;
  }

  @OnClick({R.id.iv_slide, R.id.tv_add_cover})
  void addImage() {
    if (ivCover.getDrawable() == null) {
      onAddImageListener.onAddImage();
    } else {
      ivCover.setImageDrawable(null);
      tvAddCoverText.setText(clickToAddString);
    }
  }

  public void setImageDrawable(Drawable drawable) {
    ivCover.setImageDrawable(drawable);
    tvAddCoverText.setText(clickToRemoveString);
  }

  public interface OnAddImageListener {
    void onAddImage();
  }
}
