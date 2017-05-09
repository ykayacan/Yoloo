package com.yoloo.android.feature.comment;

import android.view.View;
import com.yoloo.android.data.db.CommentRealm;

public interface OnMarkAsAcceptedClickListener {
  void onMarkAsAccepted(View v, CommentRealm comment);
}
