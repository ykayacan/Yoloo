package com.yoloo.android.feature.models;

import android.view.View;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.yoloo.android.R;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

public class BountyButtonModel extends SimpleEpoxyModel {

  @EpoxyAttribute(DoNotHash) OnBountyClickListener onBountyClickListener;

  public BountyButtonModel() {
    super(R.layout.item_feed_bounty_button);
  }

  @Override
  public void bind(View view) {
    super.bind(view);
    view.setOnClickListener(v -> onBountyClickListener.onBountyClickListener(v));
  }

  @Override
  public void unbind(View view) {
    super.unbind(view);
    view.setOnClickListener(null);
  }

  public interface OnBountyClickListener {
    void onBountyClickListener(View v);
  }
}
