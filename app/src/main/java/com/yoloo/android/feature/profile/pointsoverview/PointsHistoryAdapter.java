package com.yoloo.android.feature.profile.pointsoverview;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.yoloo.android.R;
import com.yoloo.android.data.db.GameHistoryRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import java.util.List;

class PointsHistoryAdapter extends EpoxyAdapter {

  void addHistoryData(List<GameHistoryRealm> histories) {
    for (GameHistoryRealm history : histories) {
      addModel(createModel(history));
    }
  }

  private PointsHistoryAdapter$PointHistoryModel_ createModel(GameHistoryRealm history) {
    return new PointsHistoryAdapter$PointHistoryModel_().gameHistory(history);
  }

  @EpoxyModelClass(layout = R.layout.item_point_history)
  static abstract class PointHistoryModel
      extends EpoxyModelWithHolder<PointHistoryModel.PointHistoryHolder> {

    @EpoxyAttribute GameHistoryRealm gameHistory;

    @Override public void bind(PointHistoryHolder holder) {
      final Context context = holder.itemView.getContext();
      Resources res = context.getResources();

      holder.tvEarnedPoints.setText(
          res.getString(R.string.label_earned_points, gameHistory.getPoints()));

      holder.tvEarnedBounties.setVisibility(
          gameHistory.getBounties() == 0 ? View.GONE : View.VISIBLE);
      holder.tvEarnedBounties.setText(
          res.getString(R.string.label_earned_bounties, gameHistory.getBounties()));
    }

    static class PointHistoryHolder extends BaseEpoxyHolder {
      @BindView(R.id.tv_history_earned_points) TextView tvEarnedPoints;
      @BindView(R.id.tv_history_earned_bounties) TextView tvEarnedBounties;
    }
  }
}
