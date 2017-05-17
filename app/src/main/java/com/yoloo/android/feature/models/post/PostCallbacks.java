package com.yoloo.android.feature.models.post;

import android.support.annotation.NonNull;
import android.view.View;
import com.yoloo.android.data.db.MediaRealm;
import com.yoloo.android.data.db.PostRealm;

public interface PostCallbacks {

  void onPostClickListener(@NonNull PostRealm post);

  void onPostContentImageClickListener(@NonNull MediaRealm media);

  void onPostProfileClickListener(@NonNull String userId);

  void onPostBookmarkClickListener(@NonNull PostRealm post);

  void onPostOptionsClickListener(View v, @NonNull PostRealm post);

  void onPostShareClickListener(@NonNull PostRealm post);

  void onPostCommentClickListener(@NonNull PostRealm post);

  void onPostVoteClickListener(@NonNull PostRealm post, int direction);

  void onPostTagClickListener(@NonNull String tagName);
}
