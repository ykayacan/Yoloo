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
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.CountUtil;
import java.util.List;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

public class TagListEpoxyController extends TypedEpoxyController<List<TagRealm>> {

  private OnItemClickListener<TagRealm> onItemClickListener;

  public void setOnItemClickListener(OnItemClickListener<TagRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  @Override
  protected void buildModels(List<TagRealm> tags) {
    Stream.of(tags).forEach(this::createTagModel);
  }

  private void createTagModel(TagRealm tag) {
    new TagListEpoxyController$TagListModel_()
        .id(tag.getId())
        .tagName(tag.getName())
        .postCount(CountUtil.formatCount(tag.getPostCount()))
        .onClickListener(v -> onItemClickListener.onItemClick(null, tag))
        .addTo(this);
  }

  @EpoxyModelClass(layout = R.layout.item_group_tag)
  static abstract class TagListModel extends EpoxyModel<TextView> {
    @EpoxyAttribute String tagName;
    @EpoxyAttribute String postCount;
    @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;

    @Override
    public void bind(TextView view) {
      super.bind(view);
      view.setText(view.getResources().getString(R.string.all_tag_name, tagName, postCount));
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
