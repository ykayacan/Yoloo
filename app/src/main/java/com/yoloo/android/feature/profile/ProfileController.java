package com.yoloo.android.feature.profile;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bluelinelabs.conductor.support.ControllerPagerAdapter;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.feed.postfeed.PostController;
import com.yoloo.android.feature.follow.FollowController;
import com.yoloo.android.feature.notification.NotificationController;
import com.yoloo.android.feature.search.SearchController;
import com.yoloo.android.feature.ui.widget.materialbadge.MenuItemBadge;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.ViewUtil;
import com.yoloo.android.util.glide.CropCircleTransformation;
import java.util.List;

public class ProfileController extends MvpController<ProfileView, ProfilePresenter>
    implements ProfileView {

  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.toolbar_profile) Toolbar toolbar;
  @BindView(R.id.tv_profile_title) TextView tvTitle;
  @BindView(R.id.iv_profile_avatar) ImageView ivProfileAvatar;
  @BindView(R.id.iv_profile_bg) ImageView ivProfileBg;
  @BindView(R.id.tv_profile_realname) TextView tvRealname;
  @BindView(R.id.tv_profile_level) TextView tvLevel;
  @BindView(R.id.tv_profile_posts) TextView tvPosts;
  @BindView(R.id.tv_profile_followers) TextView tvFollowers;
  @BindView(R.id.tv_profile_following) TextView tvFollowing;
  @BindView(R.id.btn_profile_follow) Button btnFollow;
  @BindView(R.id.tv_profile_bounties) TextView tvBounties;
  @BindView(R.id.tv_profile_points) TextView tvPoints;
  @BindView(R.id.tv_profile_achievements) TextView tvAchievements;

  @BindView(R.id.tablayout_profile) TabLayout tabLayout;
  @BindView(R.id.viewpager_profile) ViewPager viewPager;

  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.primary_blue) int primaryBlueColor;

  private String userId;

  private AccountRealm account;

  public ProfileController(@Nullable Bundle args) {
    super(args);
  }

  public static ProfileController create(String userId) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_USER_ID, userId)
        .build();

    return new ProfileController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_profile, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    ViewUtil.setStatusBarColor(getActivity(), Color.BLACK);
    setHasOptionsMenu(true);
    setupToolbar();

    userId = getArgs().getString(KEY_USER_ID);

    final ProfilePagerAdapter pagerAdapter =
        new ProfilePagerAdapter(this, true, getResources(), userId);

    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    getPresenter().loadUserProfile(userId);
  }

  @Override protected void onDestroyView(@NonNull View view) {
    viewPager.setAdapter(null);
    super.onDestroyView(view);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ViewUtil.setStatusBarColor(getActivity(), primaryDarkColor);
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_feed_user, menu);

    DrawableHelper.withContext(getActivity()).withColor(android.R.color.white).applyTo(menu);

    MenuItem menuMessage = menu.findItem(R.id.action_feed_message);
    MenuItemBadge.update(getActivity(), menuMessage, new MenuItemBadge.Builder());
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    switch (itemId) {
      case android.R.id.home:
        getRouter().popCurrentController();
        return true;
      case R.id.action_feed_search:
        startTransaction(new SearchController(), new VerticalChangeHandler());
        return true;
      case R.id.action_feed_message:
        MenuItemBadge.update(getActivity(), item, new MenuItemBadge.Builder()
            .iconDrawable(AppCompatResources.getDrawable(getApplicationContext(),
                R.drawable.ic_email_black_24dp))
            .textBackgroundColor(primaryBlueColor));

        MenuItemBadge.getBadgeTextView(item).setBadgeCount(23, true);
        return true;
      case R.id.action_feed_notification:
        startTransaction(new NotificationController(), new VerticalChangeHandler());
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @NonNull @Override public ProfilePresenter createPresenter() {
    return new ProfilePresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()));
  }

  @Override public void onProfileLoaded(AccountRealm account) {
    this.account = account;
    setupProfileInfo(account);
  }

  @Override public void onPostsLoaded(Response<List<PostRealm>> response) {

  }

  @Override public void onError(Throwable t) {

  }

  @OnClick(R.id.btn_profile_follow) void onFollowClick() {
    final Resources res = getResources();

    btnFollow.setText(account.isFollowing() ? res.getString(R.string.action_profile_follow)
        : res.getString(R.string.action_profile_unfollow));
    getPresenter().follow(userId, account.isFollowing() ? -1 : 1);
    account.setFollowing(!account.isFollowing());
  }

  @OnClick(R.id.card_profile_points) void onPointsCardClick() {

  }

  @OnClick(R.id.card_profile_bounties) void onBountiesCardClick() {

  }

  @OnClick(R.id.card_profile_achievements) void onAchievementsClick() {

  }

  @OnClick(R.id.tv_profile_followers) void onFollowersClick() {
    startTransaction(FollowController.create(userId, FollowController.TYPE_FOLLOWERS),
        new VerticalChangeHandler());
  }

  @OnClick(R.id.tv_profile_following) void onFollowingClick() {
    startTransaction(FollowController.create(userId, FollowController.TYPE_FOLLOWINGS),
        new VerticalChangeHandler());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  public void setupProfileInfo(AccountRealm account) {
    final Resources res = getResources();

    tvTitle.setText(account.getUsername());

    Glide.with(getActivity())
        .load(account.getAvatarUrl())
        .bitmapTransform(CropCircleTransformation.getInstance(getActivity()))
        .into(ivProfileAvatar);

    tvRealname.setText(account.getRealname());
    tvLevel.setText(res.getString(R.string.label_profile_level, account.getLevel()));
    DrawableHelper.withContext(getActivity())
        .withDrawable(tvLevel.getCompoundDrawables()[0])
        .withColor(R.color.primary_yellow)
        .tint();

    formatCounterText(tvPosts, account.getQuestions(), R.string.label_profile_posts);
    formatCounterText(tvFollowers, account.getFollowers(), R.string.label_profile_followers);
    formatCounterText(tvFollowing, account.getFollowings(), R.string.label_profile_following);

    tvPoints.setText(
        res.getString(R.string.label_profile_points, CountUtil.format(account.getPoints())));
    tvBounties.setText(res.getString(R.string.label_profile_bounties, account.getBounties()));
    tvAchievements.setText(
        res.getString(R.string.label_profile_achievements, account.getAchievements()));

    btnFollow.setVisibility(account.isMe() ? View.GONE : View.VISIBLE);
    btnFollow.setText(
        account.isFollowing() ? R.string.action_profile_unfollow : R.string.action_profile_follow);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void formatCounterText(TextView view, long value, @StringRes int stringRes) {
    final int color = ContextCompat.getColor(getActivity(), R.color.editor_icon);

    String original = getResources().getString(stringRes, CountUtil.format(value));

    final int i = original.indexOf("\n") + 1;

    Spannable span = Spannable.Factory.getInstance().newSpannable(original);
    span.setSpan(new ForegroundColorSpan(color), i, original.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    view.setText(span, TextView.BufferType.SPANNABLE);
  }

  private static class ProfilePagerAdapter extends ControllerPagerAdapter {

    private final Resources resources;
    private final String userId;

    ProfilePagerAdapter(Controller host, boolean saveControllerState, Resources resources,
        String userId) {
      super(host, saveControllerState);
      this.resources = resources;
      this.userId = userId;
    }

    @Override public Controller getItem(int position) {
      switch (position) {
        case 0:
          return PostController.ofUser(userId, false);
        case 1:
          return PostController.ofUser(userId, true);
        default:
          return null;
      }
    }

    @Override public int getCount() {
      return 2;
    }

    @Override public CharSequence getPageTitle(int position) {
      switch (position) {
        case 0:
          return resources.getString(R.string.label_profile_questions);
        case 1:
          return resources.getString(R.string.label_profile_commented);
        default:
          return null;
      }
    }
  }
}
