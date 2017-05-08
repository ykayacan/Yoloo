package com.yoloo.android.feature.recommenduser;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.feed.FeedController;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.ui.recyclerview.decoration.InsetDividerDecoration;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class RecommendUserController extends BaseController implements OnFollowClickListener {

  @BindView(R.id.recycler_view) RecyclerView rvRecommendedUsers;

  @BindColor(R.color.divider) int dividerColor;

  private RecommendUserEpoxyController epoxyController;
  private UserRepository userRepository;

  private Disposable disposable;

  public static RecommendUserController create() {
    return new RecommendUserController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_recommend_user, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupRecyclerview();

    userRepository = UserRepositoryProvider.getRepository();

    disposable = userRepository
        .listRecommendedUsers(null, 10)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(response -> Timber.d("Response: %s", response.getData()))
        .filter(response -> !response.getData().isEmpty())
        .subscribe(response -> epoxyController.setData(response.getData(), null), Timber::e);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override
  public void onFollowClick(View v, AccountRealm account, int direction) {
    userRepository.relationship(account.getId(), direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
          epoxyController.remove(account);
        }, Timber::e);

    if (epoxyController.getAdapter().getItemCount() == 0) {
      getRouter().setRoot(RouterTransaction
          .with(FeedController.create())
          .pushChangeHandler(new HorizontalChangeHandler()));
    }
  }

  @OnClick(R.id.tv_skip)
  void onSkip() {
    getRouter().setRoot(RouterTransaction
        .with(FeedController.create())
        .pushChangeHandler(new HorizontalChangeHandler()));
  }

  private void setupRecyclerview() {
    epoxyController = new RecommendUserEpoxyController(getActivity());
    epoxyController.setOnFollowClickListener(this);

    rvRecommendedUsers.setHasFixedSize(true);
    rvRecommendedUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvRecommendedUsers.setItemAnimator(new SlideInItemAnimator());
    rvRecommendedUsers.addItemDecoration(new InsetDividerDecoration(R.layout.item_user,
        getResources().getDimensionPixelSize(R.dimen.divider_height),
        getResources().getDimensionPixelSize(R.dimen.keyline_1), dividerColor));
    rvRecommendedUsers.setAdapter(epoxyController.getAdapter());
  }
}
