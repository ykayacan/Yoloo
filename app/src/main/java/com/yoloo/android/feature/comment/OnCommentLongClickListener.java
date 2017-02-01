package com.yoloo.android.feature.comment;

import android.view.View;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.data.model.CommentRealm;

public interface OnCommentLongClickListener {
  void onCommentLongClick(View v, EpoxyModel<?> model, CommentRealm comment);
}
