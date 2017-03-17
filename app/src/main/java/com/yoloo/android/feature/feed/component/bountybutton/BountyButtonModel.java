package com.yoloo.android.feature.feed.component.bountybutton;

import android.view.View;
import android.widget.Button;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.yoloo.android.R;

@EpoxyModelClass(layout = R.layout.item_feed_bounty_button)
public abstract class BountyButtonModel extends EpoxyModel<Button> {

  @EpoxyAttribute(hash = false) OnBountyButtonClickListener onBountyButtonClickListener;

  @Override public void bind(Button view) {
    view.setText(R.string.action_feed_bounty_questions);
    view.setOnClickListener(v -> onBountyButtonClickListener.onBountyButtonClick(v));
  }

  public interface OnBountyButtonClickListener {
    void onBountyButtonClick(View v);
  }
}
