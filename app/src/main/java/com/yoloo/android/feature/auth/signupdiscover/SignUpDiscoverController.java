package com.yoloo.android.feature.auth.signupdiscover;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindArray;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.annimon.stream.Stream;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.igalata.bubblepicker.BubblePickerListener;
import com.igalata.bubblepicker.adapter.BubblePickerAdapter;
import com.igalata.bubblepicker.model.BubbleGradient;
import com.igalata.bubblepicker.model.PickerItem;
import com.igalata.bubblepicker.rendering.BubblePicker;
import com.yoloo.android.R;
import com.yoloo.android.data.repository.notification.NotificationRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.IdpResponse;
import com.yoloo.android.feature.auth.InfoBundle;
import com.yoloo.android.feature.recommenduser.RecommendUserController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.LocaleUtil;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

public class SignUpDiscoverController
    extends MvpController<SignUpDiscoverView, SignUpDiscoverPresenter>
    implements SignUpDiscoverView {

  private static final String KEY_INFO_BUNDLE = "INFO_BUNDLE";
  private static final String KEY_IDP_RESPONSE = "IDP_RESPONSE";

  private static final ButterKnife.Setter<View, Boolean> VISIBLE =
      (view, value, index) -> view.setVisibility(value ? View.VISIBLE : View.INVISIBLE);

  private static final ButterKnife.Setter<View, Boolean> INVISIBLE =
      (view, value, index) -> view.setVisibility(value ? View.INVISIBLE : View.VISIBLE);

  private static final int[] TYPE_DRAWABLE_IDS = {
      R.drawable.escapist,
      R.drawable.guidebook_memorizer,
      R.drawable.know_it_all,
      R.drawable.no_expense,
      R.drawable.partier,
      R.drawable.planner,
      R.drawable.repeater,
      R.drawable.solo_traveler,
      R.drawable.thrill_seeker,
      R.drawable.travel_mate_seeker
  };

  @BindView(R.id.bubblepicker) BubblePicker picker;
  @BindView(R.id.btn_sign_up_init_get_started) TextView tvGetStarted;

  @BindArray(R.array.colors) int[] colors;
  @BindArray(R.array.traveler_types_titles) String[] travelerTypeTitles;
  @BindArray(R.array.traveler_types_ids) String[] travelerTypeIds;

  @BindString(R.string.label_loading) String loadingString;
  @BindString(R.string.error_email_already_taken) String errorEmailAlreadyTakenString;
  @BindString(R.string.error_server_down) String errorServerDownString;
  @BindString(R.string.error_already_registered) String errorAlreadyRegisteredString;

  private ArrayMap<String, String> types = new ArrayMap<>();

  private ArrayList<String> selectedTypeIds = new ArrayList<>();

  private ProgressDialog progressDialog;

  public SignUpDiscoverController(Bundle args) {
    super(args);
  }

  public static SignUpDiscoverController createWithProvider(@NonNull IdpResponse response,
      @NonNull InfoBundle infoBundle) {
    final Bundle bundle = new BundleBuilder().putParcelable(KEY_IDP_RESPONSE, response)
        .putParcelable(KEY_INFO_BUNDLE, infoBundle)
        .build();

    return new SignUpDiscoverController(bundle);
  }

  public static SignUpDiscoverController createWithEmail(@NonNull InfoBundle infoBundle) {
    final Bundle bundle = new BundleBuilder().putParcelable(KEY_INFO_BUNDLE, infoBundle).build();

    return new SignUpDiscoverController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_sign_up_discover, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    KeyboardUtil.hideKeyboard(view);

    setBubblePicker();
  }

  private void setBubblePicker() {
    Stream.of(travelerTypeTitles)
        .forEachIndexed((index, s) -> types.put(travelerTypeTitles[index], travelerTypeIds[index]));

    picker.setVisibility(View.VISIBLE);

    picker.setAdapter(new BubblePickerAdapter() {
      @Override public int getTotalCount() {
        return travelerTypeTitles.length;
      }

      @NotNull @Override public PickerItem getItem(int position) {
        int num = new Random().nextInt(4);
        return getPickerItem(getDrawable(TYPE_DRAWABLE_IDS[position]), travelerTypeTitles[position],
            colors[num]);
      }
    });

    picker.setBubbleSize(50);
    picker.setListener(new BubblePickerListener() {
      @Override public void onBubbleSelected(@NonNull PickerItem pickerItem) {
        if (types.containsKey(pickerItem.getTitle())) {
          selectedTypeIds.add(types.get(pickerItem.getTitle()));

          ButterKnife.apply(tvGetStarted, VISIBLE, selectedTypeIds.size() >= 3);
        }
      }

      @Override public void onBubbleDeselected(@NonNull PickerItem pickerItem) {
        if (types.containsKey(pickerItem.getTitle())) {
          selectedTypeIds.remove(types.get(pickerItem.getTitle()));

          ButterKnife.apply(tvGetStarted, INVISIBLE, selectedTypeIds.size() < 3);
        }
      }
    });
  }

  private Drawable getDrawable(int typeDrawableId) {
    return ContextCompat.getDrawable(getActivity(), typeDrawableId);
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    tvGetStarted.setVisibility(View.GONE);
  }

  @Override protected void onActivityResumed(@NonNull Activity activity) {
    super.onActivityResumed(activity);
    picker.onResume();
  }

  @Override protected void onActivityPaused(@NonNull Activity activity) {
    super.onActivityPaused(activity);
    picker.onPause();
  }

  @NonNull private PickerItem getPickerItem(Drawable drawable, String title, int color) {
    PickerItem item = new PickerItem();
    item.setTitle(title);
    item.setGradient(new BubbleGradient(color, color));
    item.setTypeface(Typeface.DEFAULT_BOLD);
    item.setTextColor(Color.WHITE);
    item.setTextSize(45.0F);
    item.setBackgroundImage(drawable);
    return item;
  }

  @OnClick(R.id.btn_sign_up_init_get_started) void showSignUpScreen() {
    KeyboardUtil.hideKeyboard(getView());

    if (selectedTypeIds.isEmpty()) {
      Snackbar.make(getView(), R.string.signup_discover_empty_typeid_error, Snackbar.LENGTH_SHORT)
          .show();
      return;
    }

    InfoBundle bundle = getArgs().getParcelable(KEY_INFO_BUNDLE);
    IdpResponse response = getArgs().getParcelable(KEY_IDP_RESPONSE);

    String locale = LocaleUtil.getCurrentLocale(getActivity()).getISO3Country();

    String countryCode = getCountryCode();

    if (response == null) {
      getPresenter().signUpWithPassword(bundle.getFullname(), bundle.getUsername(),
          bundle.getEmail(), bundle.getPassword(), countryCode, locale, selectedTypeIds);
    } else {
      getPresenter().signUpWithProvider(response, bundle.getUsername(), countryCode, locale,
          selectedTypeIds);
    }
  }

  @Override public void onError(Throwable t) {
    Timber.d(t);

    if (t instanceof SocketTimeoutException) {
      Snackbar.make(getView(), errorServerDownString, Snackbar.LENGTH_LONG).show();
    }

    if (t.getMessage().contains("400") || t instanceof FirebaseAuthUserCollisionException) {
      Snackbar.make(getView(), errorEmailAlreadyTakenString, Snackbar.LENGTH_LONG).show();
    }

    if (t.getMessage().contains("409")) {
      Toast.makeText(getActivity(), errorAlreadyRegisteredString, Toast.LENGTH_SHORT).show();
    }

    // Backend didn't processed, sign out account.
    AuthUI.getInstance().signOut((FragmentActivity) getActivity());
  }

  @Override public void onShowLoading() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getActivity());
      progressDialog.setMessage(loadingString);
      progressDialog.setIndeterminate(true);
    }

    progressDialog.show();
  }

  @Override public void onHideLoading() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override public void onSignedUp() {
    getRouter().pushController(RouterTransaction.with(RecommendUserController.create())
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }

  @NonNull @Override public SignUpDiscoverPresenter createPresenter() {
    return new SignUpDiscoverPresenter(UserRepositoryProvider.getRepository(),
        NotificationRepositoryProvider.getRepository());
  }

  private String getCountryCode() {
    TelephonyManager tm =
        (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
    return tm.getNetworkCountryIso();
  }
}
