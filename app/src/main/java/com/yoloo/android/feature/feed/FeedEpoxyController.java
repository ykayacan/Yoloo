package com.yoloo.android.feature.feed;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.NewUserListFeedItem;
import com.yoloo.android.data.feed.NewUserWelcomeFeedItem;
import com.yoloo.android.feature.models.NewUserWelcome_;
import com.yoloo.android.feature.postlist.PostListEpoxyController;

class FeedEpoxyController extends PostListEpoxyController {

  private View.OnClickListener onNewUserWelcomeClickListener;

  FeedEpoxyController(Context context) {
    super(context);
  }

  void setOnNewUserWelcomeClickListener(View.OnClickListener onNewUserWelcomeClickListener) {
    this.onNewUserWelcomeClickListener = onNewUserWelcomeClickListener;
  }

  void deleteNewUser(@NonNull AccountRealm account) {
    for (FeedItem<?> item : items) {
      if (item instanceof NewUserListFeedItem) {
        ((NewUserListFeedItem) item).getItem().remove(account);
        setData(items, false);
        break;
      }
    }
  }

  @Override protected void onMoreFeedItemType(FeedItem<?> item) {
    if (item instanceof NewUserWelcomeFeedItem) {
      createNewUserWelcomeItem(((NewUserWelcomeFeedItem) item).getItem());
    }
  }

  private void createNewUserWelcomeItem(AccountRealm account) {
    new NewUserWelcome_()
        .id(account.getId())
        .account(account)
        .glide(glide)
        .transformation(bitmapTransformation)
        .onClickListener(onNewUserWelcomeClickListener)
        .addTo(this);
  }
}
