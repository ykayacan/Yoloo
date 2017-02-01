package com.yoloo.android.feature.postdetail.model;

import android.widget.TextView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;

public class CommentCountModel extends EpoxyModel<TextView> {

  @EpoxyAttribute long counts;

  @Override protected int getDefaultLayout() {
    return R.layout.item_comment_count;
  }

  @Override public void bind(TextView view) {
    /*String countsFound = CountUtil.format(counts);
    String text = view.getContext()
        .getResources()
        .getQuantityString(R.plurals.label_comment_comment_text, (int) counts, counts);
    view.setText(countsFound + " " + text);*/
  }
}