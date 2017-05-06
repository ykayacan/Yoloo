package com.yoloo.android.feature.editor.selectgroup;

import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.feature.grouplist.GroupListEpoxyController;
import com.yoloo.android.feature.grouplist.GroupListEpoxyController$GroupListItemModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

class SelectGroupEpoxyController extends Typed2EpoxyController<List<GroupRealm>, Boolean> {

  private final RequestManager glide;

  @AutoModel SelectGroupEpoxyController$GroupHeaderModel_ header;

  private OnItemClickListener<GroupRealm> onItemClickListener;
  private GroupListEpoxyController.OnSubscribeListener onSubscribeListener;

  SelectGroupEpoxyController(RequestManager glide) {
    this.glide = glide;
  }

  public void setOnItemClickListener(OnItemClickListener<GroupRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void setOnSubscribeListener(
      GroupListEpoxyController.OnSubscribeListener onSubscribeListener) {
    this.onSubscribeListener = onSubscribeListener;
  }

  @Override
  protected void buildModels(List<GroupRealm> groups, Boolean showSubscribedHeader) {
    header.layout(R.layout.item_select_all_group_header).addTo(this);
    Stream.of(groups).forEach(this::createModel);

    //header.layout(R.layout.item_select_subscribed_group_header).addTo(this);
  }

  private void createModel(GroupRealm group) {
    new GroupListEpoxyController$GroupListItemModel_()
        .id(group.getId())
        .group(group)
        .glide(glide)
        .showNotSubscribedError(true)
        .hideSubscribeButton(group.isSubscribed())
        .onItemClickListener(onItemClickListener)
        .onSubscribeClickListener(onSubscribeListener)
        .addTo(this);
  }

  @EpoxyModelClass(layout = R.layout.item_select_all_group_header)
  static abstract class GroupHeaderModel extends SimpleEpoxyModel {
    public GroupHeaderModel() {
      super(R.layout.item_select_all_group_header);
    }
  }
}
