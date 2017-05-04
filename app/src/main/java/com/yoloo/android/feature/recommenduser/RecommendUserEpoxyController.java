package com.yoloo.android.feature.recommenduser;

import android.content.Context;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.feature.search.UserModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

public class RecommendUserEpoxyController extends Typed2EpoxyController<List<AccountRealm>, Void> {

  private final CropCircleTransformation cropCircleTransformation;

  @AutoModel FacebookFriendHeaderModel facebookFriendsModel;

  private OnFollowClickListener onFollowClickListener;

  private List<AccountRealm> models;

  public RecommendUserEpoxyController(Context context) {
    models = new ArrayList<>();
    cropCircleTransformation = new CropCircleTransformation(context);
  }

  public void setOnFollowClickListener(OnFollowClickListener onFollowClickListener) {
    this.onFollowClickListener = onFollowClickListener;
  }

  @Override
  public void setData(List<AccountRealm> models, Void data2) {
    this.models = models;
    super.setData(models, data2);
  }

  @Override
  protected void buildModels(List<AccountRealm> accounts, Void aVoid) {
    Stream
        .of(accounts)
        .forEach(account -> new UserModel_()
            .id(account.getId())
            .account(account)
            .showFollowButton(true)
            .onFollowClickListener(onFollowClickListener)
            .cropCircleTransformation(cropCircleTransformation)
            .addTo(this));

    //facebookFriendsModel.layout(R.layout.item_recommend_user_facebook_friends).addTo(this);
  }

  public void remove(AccountRealm account) {
    models.remove(account);
    setData(models, null);
  }

  public static class FacebookFriendHeaderModel extends SimpleEpoxyModel {

    public FacebookFriendHeaderModel() {
      super(R.layout.item_recommend_user_facebook_friends);
    }
  }
}
