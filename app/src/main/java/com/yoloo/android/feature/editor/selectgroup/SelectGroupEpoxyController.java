package com.yoloo.android.feature.editor.selectgroup;

import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.feature.grouplist.GroupListEpoxyController;
import com.yoloo.android.feature.grouplist.GroupListEpoxyController$GroupListItemModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.EpoxyItem;
import java.util.List;

class SelectGroupEpoxyController extends TypedEpoxyController<List<EpoxyItem<?>>> {

  private final RequestManager glide;

  @AutoModel SelectGroupEpoxyController$GroupHeaderModel_ headerAllModel;
  @AutoModel SelectGroupEpoxyController$GroupHeaderModel_ headerSubscribedModel;

  private OnItemClickListener<GroupRealm> onItemClickListener;
  private GroupListEpoxyController.OnSubscribeListener onSubscribeListener;

  SelectGroupEpoxyController(RequestManager glide) {
    this.glide = glide;
  }

  public void setOnItemClickListener(OnItemClickListener<GroupRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  void setOnSubscribeListener(GroupListEpoxyController.OnSubscribeListener onSubscribeListener) {
    this.onSubscribeListener = onSubscribeListener;
  }

  @Override
  protected void buildModels(List<EpoxyItem<?>> groups) {
    Stream.of(groups).forEach(item -> {
      if (item instanceof SelectGroupPresenter.SubscribedHeader) {
        headerAllModel.layout(R.layout.item_select_all_group_header).addTo(this);
      } else if (item instanceof SelectGroupPresenter.AllHeader) {
        headerSubscribedModel.layout(R.layout.item_select_subscribed_group_header).addTo(this);
      } else if (item instanceof SelectGroupPresenter.GroupItem) {
        createModel(((SelectGroupPresenter.GroupItem) item).getItem());
      }
    });
  }

  private void createModel(GroupRealm group) {
    new GroupListEpoxyController$GroupListItemModel_()
        .id(group.getId())
        .group(group)
        .glide(glide)
        .showNotSubscribedError(true)
        .onItemClickListener(onItemClickListener)
        .onSubscribeClickListener(onSubscribeListener)
        .addTo(this);
  }

  @EpoxyModelClass(layout = R.layout.item_select_all_group_header)
  static abstract class GroupHeaderModel extends SimpleEpoxyModel {
    GroupHeaderModel() {
      super(R.layout.item_select_all_group_header);
    }
  }
}
