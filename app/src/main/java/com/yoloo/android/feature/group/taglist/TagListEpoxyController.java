package com.yoloo.android.feature.group.taglist;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.R;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

public class TagListEpoxyController extends TypedEpoxyController<List<String>> {

  private OnItemClickListener<String> onItemClickListener;

  public void setOnItemClickListener(OnItemClickListener<String> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  @Override
  protected void buildModels(List<String> tagNames) {
    Stream.of(tagNames).forEach(this::createTagModel);
  }

  private void createTagModel(String tagName) {
    new TagListEpoxyController$TagListModel_()
        .id(tagName)
        .tagName(tagName)
        .onClickListener(v -> onItemClickListener.onItemClick(null, tagName))
        .addTo(this);
  }

  @EpoxyModelClass(layout = R.layout.item_autocomplete_tag)
  static abstract class TagListModel extends EpoxyModel<TextView> {
    @EpoxyAttribute String tagName;
    @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;

    @Override
    public void bind(TextView view) {
      super.bind(view);
      view.setText(tagName);
      view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.chip_tag_bg));
      view.setOnClickListener(onClickListener);
    }

    @Override
    public void unbind(TextView view) {
      super.unbind(view);
      view.setOnClickListener(null);
    }
  }
}
