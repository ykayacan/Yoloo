package com.yoloo.android.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.yoloo.android.R;

public class BlogView extends LinearLayout {

  @BindView(R.id.tv_blog_title) TextView tvBlogTitle;
  @BindView(R.id.tv_blog_user_info) TextView tvBlogUserInfo;
  @BindView(R.id.tv_blog_content) TextView tvBlogContent;

  public BlogView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    inflate(getContext(), R.layout.layout_blogview, this);

    ButterKnife.bind(this);

    setOrientation(LinearLayout.VERTICAL);
  }

  public void setBlogTitle(@NonNull String title) {
    tvBlogTitle.setText(title);
  }

  public void setBlogUserInfo(@NonNull String username, @NonNull String levelTitle) {
    tvBlogUserInfo.setText(
        getResources().getString(R.string.label_blog_username_info, username, levelTitle));
  }

  public void setBlogContent(@NonNull String content) {
    tvBlogContent.setText(content);
  }
}
