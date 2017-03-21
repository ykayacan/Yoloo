package com.yoloo.android.feature.profile;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bluelinelabs.conductor.support.RouterPagerAdapter;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.category.CategoryController;
import com.yoloo.android.feature.feed.global.FeedGlobalController;
import com.yoloo.android.feature.follow.FollowController;
import com.yoloo.android.feature.profile.photos.PhotosController;
import com.yoloo.android.feature.profile.profileedit.ProfileEditController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.Pair;
import com.yoloo.android.util.VersionUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ProfileController extends MvpController<ProfileView, ProfilePresenter>
    implements ProfileView {

  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.toolbar_profile) Toolbar toolbar;
  @BindView(R.id.iv_profile_edit) ImageView ivEdit;
  @BindView(R.id.iv_profile_avatar) ImageView ivProfileAvatar;
  @BindView(R.id.iv_profile_bg) ImageView ivProfileBg;
  @BindView(R.id.viewstub_profile_info) ViewStub stubInfo;
  @BindView(R.id.tv_profile_realname) TextView tvRealname;
  @BindView(R.id.tv_profile_username) TextView tvUsername;
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

  @BindString(R.string.label_profile_tab_posts) String profilePostsTabString;
  @BindString(R.string.label_profile_tab_photos) String profilePhotosTabString;
  @BindString(R.string.label_profile_tab_countries) String profileCountriesTabString;
  @BindString(R.string.label_profile_tab_interests) String profileInterestsTabString;

  private String userId;

  private AccountRealm account;

  public ProfileController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static ProfileController create(@NonNull String userId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_USER_ID, userId).build();

    return new ProfileController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_profile, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setHasOptionsMenu(true);
    setupToolbar();

    userId = getArgs().getString(KEY_USER_ID);

    List<Pair<String, Controller>> pairs = new ArrayList<>(4);
    pairs.add(Pair.create(profilePostsTabString, FeedGlobalController.ofUser(userId, false)));
    pairs.add(Pair.create(profilePhotosTabString, PhotosController.create(userId)));
    pairs.add(Pair.create(profileCountriesTabString, PhotosController.create(userId)));
    pairs.add(Pair.create(profileInterestsTabString, CategoryController.create(userId, 3)));
    pairs.add(Pair.create(profileInterestsTabString, PhotosController.create(userId)));

    final RouterPagerAdapter pagerAdapter = new ProfilePagerAdapter(this, pairs);

    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);
  }

  @Override protected void onAttach(@NonNull View view) {
    ViewUtils.setStatusBarColor(getActivity(), Color.BLACK);
    getPresenter().loadUserProfile(userId);
  }

  @Override protected void onDetach(@NonNull View view) {
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);
  }

  @Override protected void onDestroyView(@NonNull View view) {
    viewPager.setAdapter(null);
    super.onDestroyView(view);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      getRouter().handleBack();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @NonNull @Override public ProfilePresenter createPresenter() {
    return new ProfilePresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance())
    );
  }

  @Override public void onProfileLoaded(AccountRealm account) {
    this.account = account;
    setupProfileInfo(account);
  }

  @Override public void onError(Throwable t) {
    Timber.e(t);
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

  @OnClick(R.id.iv_profile_edit) void openProfileEdit() {
    startTransaction(ProfileEditController.create(), new HorizontalChangeHandler());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void setupProfileInfo(AccountRealm account) {
    final Resources res = getResources();

    tvUsername.setText(account.getUsername());

    Glide.with(getActivity())
        .load(account.getAvatarUrl().replace("s96-c", "s80-c-rw"))
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(ivProfileAvatar);

    if (VersionUtil.hasL()) {
      ivProfileAvatar.setTransitionName(
          getResources().getString(R.string.transition_avatar));
    }

    if (!TextUtils.isEmpty(account.getBio()) || !TextUtils.isEmpty(account.getWebsiteUrl())) {
      View view = stubInfo.inflate();
      TextView tvBio = ButterKnife.findById(view, R.id.tv_profile_bio);
      tvBio.setText(account.getBio());

      TextView tvWebsite = ButterKnife.findById(view, R.id.tv_profile_website);
      tvWebsite.setText(account.getWebsiteUrl());
    }
    tvRealname.setText(account.getRealname());
    tvLevel.setText(res.getString(R.string.label_profile_level, account.getLevel()));
    DrawableHelper.create()
        .withDrawable(tvLevel.getCompoundDrawables()[0])
        .withColor(getActivity(), R.color.primary_yellow)
        .tint();

    formatCounterText(tvPosts, account.getPostCount(), R.string.label_profile_posts);
    formatCounterText(tvFollowers, account.getFollowerCount(), R.string.label_profile_followers);
    formatCounterText(tvFollowing, account.getFollowingCount(), R.string.label_profile_following);

    tvPoints.setText(
        res.getString(R.string.label_profile_points,
            CountUtil.formatCount(account.getPointCount())));
    tvBounties.setText(res.getString(R.string.label_profile_bounties, account.getBountyCount()));
    tvAchievements.setText(
        res.getString(R.string.label_profile_countries, account.getAchievementCount()));

    btnFollow.setVisibility(account.isMe() ? View.GONE : View.VISIBLE);
    btnFollow.setText(account.isFollowing()
        ? R.string.action_profile_unfollow
        : R.string.action_profile_follow);

    ivEdit.setVisibility(account.isMe() ? View.VISIBLE : View.GONE);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void formatCounterText(TextView view, long value, @StringRes int stringRes) {
    String original = getResources().getString(stringRes, CountUtil.formatCount(value));

    final int i = original.indexOf("\n") + 1;

    Spannable span = Spannable.Factory.getInstance().newSpannable(original);
    span.setSpan(new StyleSpan(Typeface.BOLD), i, original.length(),
        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    span.setSpan(new ForegroundColorSpan(Color.WHITE), i, original.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    view.setText(span, TextView.BufferType.SPANNABLE);
  }

  private static class ProfilePagerAdapter extends RouterPagerAdapter {
    private final List<Pair<String, Controller>> pairs;

    ProfilePagerAdapter(@NonNull Controller host, List<Pair<String, Controller>> pairs) {
      super(host);
      this.pairs = pairs;
    }

    @Override public int getCount() {
      return pairs.size();
    }

    @Override public CharSequence getPageTitle(int position) {
      return pairs.get(position).first;
    }

    @Override public void configureRouter(@NonNull Router router, int position) {
      if (!router.hasRootController()) {
        router.setRoot(RouterTransaction.with(pairs.get(position).second));
      }
    }
  }
}
