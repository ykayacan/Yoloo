package com.yoloo.android.feature.models;

import android.view.View;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.yoloo.android.R;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

public class BountyButtonModel extends SimpleEpoxyModel {

  @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;

  public BountyButtonModel() {
    super(R.layout.item_feed_bounty_button);
  }

  @Override
  public void bind(View view) {
    super.bind(view);
    view.setOnClickListener(onClickListener);
  }

  @Override
  public void unbind(View view) {
    super.unbind(view);
    view.setOnClickListener(null);
  }
}
