package com.yoloo.android.feature.group.taglist;

import android.widget.TextView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.yoloo.android.R;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class TagListAdapter extends EpoxyAdapter {

  private OnItemClickListener<String> onItemClickListener;

  public void addTags(List<String> tagNames) {
    for (String tagName : tagNames) {
      addModel(new TagListAdapter$TagListModel_()
          .tagName(tagName)
          .onItemClickListener(onItemClickListener));
    }
  }

  public void setOnItemClickListener(OnItemClickListener<String> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  @EpoxyModelClass(layout = R.layout.item_autocomplete_tag)
  static abstract class TagListModel extends EpoxyModel<TextView> {
    @EpoxyAttribute String tagName;
    @EpoxyAttribute(hash = false) OnItemClickListener<String> onItemClickListener;

    @Override
    public void bind(TextView view) {
      super.bind(view);
      view.setText(tagName);

      view.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, tagName));
    }

    @Override
    public void unbind(TextView view) {
      super.unbind(view);
      view.setOnClickListener(null);
    }
  }
}
