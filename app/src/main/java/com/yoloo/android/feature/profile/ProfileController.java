package com.yoloo.android.feature.profile;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bluelinelabs.conductor.support.RouterPagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CountryRealm;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.follow.FollowController;
import com.yoloo.android.feature.groupgridoverview.GroupGridOverviewController;
import com.yoloo.android.feature.postlist.PostListController;
import com.yoloo.android.feature.profile.photolist.PhotoListController;
import com.yoloo.android.feature.profile.pointsoverview.PointsOverviewController;
import com.yoloo.android.feature.profile.profileedit.ProfileEditController;
import com.yoloo.android.feature.profile.visitedcountrylist.VisitedCountryListController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.CountUtil;
import com.yoloo.android.util.Pair;
import com.yoloo.android.util.TextViewUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import timber.log.Timber;

public class ProfileController extends MvpController<ProfileView, ProfilePresenter>
    implements ProfileView {

  private static final String KEY_USER_ID = "USER_ID";

  private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 15;

  @BindView(R.id.appbar_profile) AppBarLayout appBarLayout;
  @BindView(R.id.toolbar_profile) Toolbar toolbar;
  @BindView(R.id.iv_profile_action) ImageView ivProfileAction;
  @BindView(R.id.iv_profile_avatar) ImageView ivProfileAvatar;
  @BindView(R.id.iv_profile_bg) ImageView ivProfileBg;
  @BindView(R.id.tv_profile_realname) TextView tvRealname;
  @BindView(R.id.tv_profile_username) TextView tvUsername;
  @BindView(R.id.tv_profile_level) TextView tvLevel;
  @BindView(R.id.tv_profile_country) TextView tvCountry;
  @BindView(R.id.tv_profile_bio) TextView tvBio;
  @BindView(R.id.tv_profile_website) TextView tvWebsiteUrl;
  @BindView(R.id.tv_profile_followers_counter) TextView tvFollowerCounter;
  @BindView(R.id.tv_profile_followers_counter_text) TextView tvFollowerCounterText;
  @BindView(R.id.tv_profile_following_counter) TextView tvFollowingCounter;
  @BindView(R.id.tv_profile_following_counter_text) TextView tvFollowingCounterText;
  @BindView(R.id.btn_profile_follow) Button btnFollow;
  @BindView(R.id.tv_profile_bounty_count) TextView tvBountyCount;
  @BindView(R.id.tv_profile_point_count) TextView tvPointCount;
  @BindView(R.id.tv_profile_country_count) TextView tvCountryCount;

  @BindView(R.id.tablayout_profile) TabLayout tabLayout;
  @BindView(R.id.viewpager_profile) ViewPager viewPager;

  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.primary_blue) int primaryBlueColor;
  @BindColor(android.R.color.primary_text_light) int primaryTextLightColor;

  @BindString(R.string.label_profile_tab_posts) String profilePostsTabString;
  @BindString(R.string.label_profile_tab_photos) String profilePhotosTabString;
  @BindString(R.string.label_profile_tab_interests) String profileInterestsTabString;

  @BindDrawable(R.drawable.ic_settings_white_24dp) Drawable settingDrawable;
  @BindDrawable(R.drawable.ic_email_outline) Drawable messageDrawable;

  private AccountRealm account;

  private boolean isAvatarShown = true;
  private int maxScrollSize;

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
    setupToolbar();
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    final String userId = getArgs().getString(KEY_USER_ID);

    List<Pair<String, Controller>> pairs = new ArrayList<>(4);
    pairs.add(Pair.create(profilePostsTabString, PostListController.ofUser(userId)));
    pairs.add(Pair.create(profilePhotosTabString, PhotoListController.create(userId)));
    pairs.add(
        Pair.create(profileInterestsTabString, GroupGridOverviewController.create(userId, 3)));

    final RouterPagerAdapter pagerAdapter = new ProfilePagerAdapter(this, pairs);

    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);

    int randomNum = new Random().nextInt(7) + 1;

    final String backgroundUrl =
        "https://storage.googleapis.com/yoloo-151719.appspot.com/profile-bg-images/small/p"
            + randomNum
            + ".webp";

    Glide.with(getActivity()).load(backgroundUrl).into(ivProfileBg);

    animateAvatar();

    getPresenter().loadUserProfile(userId);
  }

  @Override protected void onDestroyView(@NonNull View view) {
    viewPager.setAdapter(null);
    super.onDestroyView(view);
  }

  @Override protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    getDrawerLayout().setFitsSystemWindows(false);
    ViewUtils.setStatusBarColor(getActivity(), Color.TRANSPARENT);
  }

  @NonNull @Override public ProfilePresenter createPresenter() {
    return new ProfilePresenter(UserRepositoryProvider.getRepository());
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
    getPresenter().follow(account.getId(), account.isFollowing() ? -1 : 1);
    account.setFollowing(!account.isFollowing());
  }

  @OnClick(R.id.card_profile_point_count) void openPointScreen() {
    startTransaction(PointsOverviewController.create(), new VerticalChangeHandler());
  }

  @OnClick(R.id.card_profile_bounty_count) void openBountyScreen() {

  }

  @OnClick(R.id.card_profile_country_count) void openVisitedCountiesScreen() {
    startTransaction(VisitedCountryListController.create(account.isMe(), account.getId()),
        new VerticalChangeHandler());
  }

  @OnClick(R.id.tv_profile_followers_counter_text) void onFollowersClick() {
    startTransaction(FollowController.create(account.getId(), FollowController.TYPE_FOLLOWERS),
        new VerticalChangeHandler());
  }

  @OnClick(R.id.tv_profile_following_counter_text) void onFollowingClick() {
    startTransaction(FollowController.create(account.getId(), FollowController.TYPE_FOLLOWINGS),
        new VerticalChangeHandler());
  }

  @OnClick(R.id.iv_profile_action) void onProfileAction() {
    if (account.isMe()) {
      startTransaction(ProfileEditController.create(), new HorizontalChangeHandler());
    } else {
      //startTransaction(ChatController.create());
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }

  private void animateAvatar() {
    maxScrollSize = appBarLayout.getTotalScrollRange();

    appBarLayout.addOnOffsetChangedListener((layout, verticalOffset) -> {
      if (maxScrollSize == 0) {
        maxScrollSize = appBarLayout.getTotalScrollRange();
      }

      int percentage = (Math.abs(verticalOffset)) * 100 / maxScrollSize;

      if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && isAvatarShown) {
        isAvatarShown = false;

        ivProfileAvatar.animate().scaleY(0).scaleX(0).setDuration(200);
      }

      if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !isAvatarShown) {
        isAvatarShown = true;

        ivProfileAvatar.animate().scaleY(1).scaleX(1);
      }
    });
  }

  private void setupProfileInfo(AccountRealm account) {
    final Resources res = getResources();

    tvUsername.setText(account.getUsername());

    Glide.with(getActivity())
        .load(account.getAvatarUrl().replace("s96-c", "s96-c-rw"))
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(ivProfileAvatar);

    CountryRealm country = account.getCountry();
    tvCountry.setText(country.getName());
    Glide.with(getActivity())
        .load(country.getFlagUrl())
        .override(24, 24)
        .into(new SimpleTarget<GlideDrawable>() {
          @Override public void onResourceReady(GlideDrawable resource,
              GlideAnimation<? super GlideDrawable> glideAnimation) {
            tvCountry.setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null);
          }
        });

    tvBio.setVisibility(TextUtils.isEmpty(account.getBio()) ? View.GONE : View.VISIBLE);
    tvBio.setText(account.getBio());

    tvWebsiteUrl.setVisibility(
        TextUtils.isEmpty(account.getWebsiteUrl()) ? View.GONE : View.VISIBLE);
    if (!TextUtils.isEmpty(account.getWebsiteUrl())) {
      tvWebsiteUrl.setText(account.getWebsiteUrl());
      TextViewUtil.stripUnderlines((Spannable) tvWebsiteUrl.getText());
    }

    tvRealname.setText(account.getRealname());
    tvLevel.setText(res.getString(R.string.label_profile_level, account.getLevel()));

    tvFollowerCounter.setText(CountUtil.formatCount(account.getFollowerCount()));
    tvFollowingCounter.setText(CountUtil.formatCount(account.getFollowingCount()));

    tvPointCount.setText(res.getString(R.string.label_profile_points,
        CountUtil.formatCount(account.getPointCount())));
    tvBountyCount.setText(res.getString(R.string.label_profile_bounties, account.getBountyCount()));
    tvCountryCount.setText(
        res.getString(R.string.label_profile_countries, account.getCountryCount()));

    btnFollow.setVisibility(account.isMe() ? View.GONE : View.VISIBLE);
    btnFollow.setText(
        account.isFollowing() ? R.string.action_profile_unfollow : R.string.action_profile_follow);

    if (account.isMe()) {
      ivProfileAction.setImageDrawable(settingDrawable);
    } else {
      ivProfileAction.setImageDrawable(messageDrawable);
      ivProfileAction.setColorFilter(Color.WHITE);
    }
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
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
