package com.yoloo.android.feature.feed.common.model;

import android.widget.Button;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.yoloo.android.R;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;

@EpoxyModelClass(layout = R.layout.item_feed_bounty_button)
public abstract class BountyButtonModel extends EpoxyModel<Button> {

  @EpoxyAttribute(hash = false) FeedAdapter.OnBountyClickListener onBountyClickListener;

  @Override public void bind(Button view) {
    super.bind(view);
    view.setText(R.string.action_feed_bounty_questions);
    view.setOnClickListener(v -> onBountyClickListener.onBountyClick(v));
  }
}