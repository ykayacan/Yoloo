package com.yoloo.android.feature.group;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bluelinelabs.conductor.support.RouterPagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.group.groupuserslist.GroupUsersListController;
import com.yoloo.android.feature.group.taglist.TagListController;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.widget.AvatarView;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.Pair;
import com.yoloo.android.util.UpdateCallback;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.glide.AvatarTarget;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class GroupController extends MvpController<GroupView, GroupPresenter> implements GroupView {

  private static final String KEY_GROUP_ID = "GROUP_ID";

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.tablayout) TabLayout tabLayout;
  @BindView(R.id.viewpager) ViewPager viewPager;
  @BindView(R.id.tv_group_title) TextView tvTitle;
  @BindView(R.id.iv_group_cover) ImageView ivBackgroundCover;
  @BindView(R.id.tv_group_subscriber_count) TextView tvSubscriberCount;
  @BindView(R.id.btn_group_subscribe) Button btnSubscribe;
  @BindView(R.id.iv_member_1) AvatarView ivMember1;
  @BindView(R.id.iv_member_2) AvatarView ivMember2;
  @BindView(R.id.iv_member_3) AvatarView ivMember3;
  @BindView(R.id.iv_member_4) AvatarView ivMember4;
  @BindView(R.id.tv_member_more) TextView tvMemberMore;

  @BindString(R.string.group_subscribe) String subscribeString;
  @BindString(R.string.group_unsubscribed) String unsubscribeString;

  @BindString(R.string.group_tab_posts) String postsTabString;
  @BindString(R.string.group_tab_tags) String tagsTabString;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private String groupId;

  private GroupRealm group;

  private boolean reEnter;

  public GroupController(@Nullable Bundle args) {
    super(args);
  }

  public static GroupController create(@NonNull String groupId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_GROUP_ID, groupId).build();

    return new GroupController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_group, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setupToolbar();
    setHasOptionsMenu(true);

    groupId = getArgs().getString(KEY_GROUP_ID);

    List<Pair<String, Controller>> pairs = new ArrayList<>(3);
    pairs.add(Pair.create(postsTabString, PostListController.ofGroup(groupId)));
    pairs.add(Pair.create(tagsTabString, TagListController.create(groupId)));

    final RouterPagerAdapter pagerAdapter = new GroupPagerAdapter(this, pairs);

    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);

    if (!reEnter) {
      getPresenter().loadGroupInfoAndPosts(groupId);
      reEnter = true;
    }
  }

  @Override
  protected void onDestroyView(@NonNull View view) {
    viewPager.setAdapter(null);
    super.onDestroyView(view);
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(GroupRealm value) {
    group = value;

    setTopGroupSubscribers(value);

    tvTitle.setText(group.getName());
    Glide.with(getActivity()).load(value.getImageWithoutIconUrl()).into(ivBackgroundCover);
    tvSubscriberCount.setText(getResources().getString(R.string.group_subscriber_count,
        CountUtil.formatCount(value.getSubscriberCount())));

    btnSubscribe.setVisibility(View.VISIBLE);
    btnSubscribe.setText(value.isSubscribed() ? unsubscribeString : subscribeString);
  }

  @Override
  public void onError(Throwable e) {
    Timber.e(e);
  }

  @Override
  public void onEmpty() {

  }

  @Override
  public void onAccountLoaded(AccountRealm account) {

  }

  @NonNull
  @Override
  public GroupPresenter createPresenter() {
    return new GroupPresenter(GroupRepositoryProvider.getRepository(),
        UserRepositoryProvider.getRepository());
  }

  @OnClick(R.id.btn_group_subscribe)
  void subscribe() {
    btnSubscribe.setText(group.isSubscribed() ? subscribeString : unsubscribeString);

    if (group.isSubscribed()) {
      long count = group.getSubscriberCount() - 1;
      group.setSubscriberCount(count);
      getPresenter().unsubscribe(groupId);
      tvSubscriberCount.setText(getResources().getString(R.string.group_subscriber_count,
          CountUtil.formatCount(group.getSubscriberCount())));
    } else {
      long count = group.getSubscriberCount() + 1;
      group.setSubscriberCount(count);
      getPresenter().subscribe(groupId);
      tvSubscriberCount.setText(getResources().getString(R.string.group_subscriber_count,
          CountUtil.formatCount(group.getSubscriberCount())));
    }

    group.setSubscribed(!group.isSubscribed());

    Controller targetController = getTargetController();
    if (targetController != null) {
      //noinspection unchecked
      ((UpdateCallback<GroupRealm>) targetController).onModelUpdated(group);
    }
  }

  @OnClick({
      R.id.iv_member_1, R.id.iv_member_2, R.id.iv_member_3, R.id.iv_member_4, R.id.tv_member_more
  })
  void onSubscribersClick() {
    getRouter().pushController(RouterTransaction.with(GroupUsersListController.create(groupId))
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
    ab.setDisplayShowTitleEnabled(false);
  }

  private void setTopGroupSubscribers(GroupRealm group) {
    final int topSubscriberCount = group.getTopSubscribers().size();
    final String count = "+" + topSubscriberCount;

    ivMember1.setVisibility(topSubscriberCount > 0 ? View.VISIBLE : View.GONE);
    ivMember2.setVisibility(topSubscriberCount > 1 ? View.VISIBLE : View.GONE);
    ivMember3.setVisibility(topSubscriberCount > 2 ? View.VISIBLE : View.GONE);
    ivMember4.setVisibility(topSubscriberCount > 3 ? View.VISIBLE : View.GONE);
    tvMemberMore.setVisibility(topSubscriberCount > 4 ? View.VISIBLE : View.GONE);
    tvMemberMore.setText(count);

    RequestManager glide = Glide.with(getActivity());

    if (topSubscriberCount > 0) {
      glide
          .load(group.getTopSubscribers().get(0).getAvatarUrl())
          .into(new AvatarTarget(ivMember1));
    }

    if (topSubscriberCount > 1) {
      glide
          .load(group.getTopSubscribers().get(1).getAvatarUrl())
          .into(new AvatarTarget(ivMember2));
    }

    if (topSubscriberCount > 2) {
      glide.load(group.getTopSubscribers().get(2).getAvatarUrl())
          .into(new AvatarTarget(ivMember3));
    }

    if (topSubscriberCount > 3) {
      glide.load(group.getTopSubscribers().get(3).getAvatarUrl())
          .into(new AvatarTarget(ivMember4));
    }
  }

  private static class GroupPagerAdapter extends RouterPagerAdapter {
    private final List<Pair<String, Controller>> pairs;

    GroupPagerAdapter(@NonNull Controller host, List<Pair<String, Controller>> pairs) {
      super(host);
      this.pairs = pairs;
    }

    @Override
    public int getCount() {
      return pairs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return pairs.get(position).first;
    }

    @Override
    public void configureRouter(@NonNull Router router, int position) {
      if (!router.hasRootController()) {
        router.setRoot(RouterTransaction.with(pairs.get(position).second));
      }
    }
  }
}
