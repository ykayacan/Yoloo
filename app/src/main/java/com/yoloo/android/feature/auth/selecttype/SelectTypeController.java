package com.yoloo.android.feature.auth.selecttype;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
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
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.igalata.bubblepicker.BubblePickerListener;
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
import com.yoloo.android.util.ViewUtils;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;
import timber.log.Timber;

public class SelectTypeController extends MvpController<SelectTypeView, SelectTypePresenter>
    implements SelectTypeView {

  private static final String KEY_INFO_BUNDLE = "INFO_BUNDLE";
  private static final String KEY_IDP_RESPONSE = "IDP_RESPONSE";

  @BindView(R.id.bubblepicker) BubblePicker picker;
  @BindView(R.id.btn_sign_up_init_get_started) TextView tvGetStarted;
  @BindView(R.id.space_workaround) View space;

  @BindArray(R.array.colors) int[] colors;
  @BindArray(R.array.traveler_types_images) TypedArray travelerTypeDrawables;
  @BindArray(R.array.traveler_types_titles) String[] travelerTypeTitles;
  @BindArray(R.array.traveler_types_ids) String[] travelerTypeIds;

  @BindString(R.string.label_loading) String loadingString;
  @BindString(R.string.error_email_already_taken) String errorEmailAlreadyTakenString;
  @BindString(R.string.error_server_down) String errorServerDownString;
  @BindString(R.string.error_already_registered) String errorAlreadyRegisteredString;

  private ArrayMap<String, String> types = new ArrayMap<>();

  private ArrayList<String> selectedTypeIds = new ArrayList<>();

  private ProgressDialog progressDialog;

  public SelectTypeController(Bundle args) {
    super(args);
  }

  public static SelectTypeController createWithProvider(@NonNull IdpResponse response,
      @NonNull InfoBundle infoBundle) {
    final Bundle bundle = new BundleBuilder()
        .putParcelable(KEY_IDP_RESPONSE, response)
        .putParcelable(KEY_INFO_BUNDLE, infoBundle)
        .build();

    return new SelectTypeController(bundle);
  }

  public static SelectTypeController createWithEmail(@NonNull InfoBundle infoBundle) {
    final Bundle bundle = new BundleBuilder().putParcelable(KEY_INFO_BUNDLE, infoBundle).build();

    return new SelectTypeController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_select_type, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    Point point = ViewUtils.getNavigationBarSize(getActivity());

    if (space != null) {
      space.setVisibility(point.y > 0 ? View.VISIBLE : View.GONE);
    }

    for (int i = 0; i < travelerTypeTitles.length; i++) {
      types.put(travelerTypeTitles[i], travelerTypeIds[i]);
    }

    ArrayList<PickerItem> items = new ArrayList<>();

    picker.setVisibility(View.VISIBLE);

    for (int i = 0; i < travelerTypeTitles.length; i++) {
      Drawable drawable = travelerTypeDrawables.getDrawable(i);
      String title = travelerTypeTitles[i];

      int num = new Random().nextInt(4);

      BubbleGradient gradient = new BubbleGradient(colors[num], colors[num]);

      PickerItem item =
          new PickerItem(title, null, true, null, gradient, 0.5F, Typeface.DEFAULT_BOLD,
              Color.WHITE, 46.0F, drawable);
      items.add(item);
    }

    travelerTypeDrawables.recycle();

    picker.setItems(items);
    picker.setBubbleSize(52);
    picker.setListener(new BubblePickerListener() {
      @Override
      public void onBubbleSelected(@NonNull PickerItem pickerItem) {
        if (types.containsKey(pickerItem.getTitle())) {
          selectedTypeIds.add(types.get(pickerItem.getTitle()));

          if (selectedTypeIds.size() >= 3) {
            tvGetStarted.setVisibility(View.VISIBLE);
          }
        }
      }

      @Override
      public void onBubbleDeselected(@NonNull PickerItem pickerItem) {
        if (types.containsKey(pickerItem.getTitle())) {
          selectedTypeIds.remove(types.get(pickerItem.getTitle()));

          if (selectedTypeIds.size() < 3) {
            tvGetStarted.setVisibility(View.INVISIBLE);
          }
        }
      }
    });
  }

  @OnClick(R.id.btn_sign_up_init_get_started)
  void showSignUpScreen() {
    InfoBundle bundle = getArgs().getParcelable(KEY_INFO_BUNDLE);
    IdpResponse response = getArgs().getParcelable(KEY_IDP_RESPONSE);

    KeyboardUtil.hideKeyboard(getView());

    String locale = LocaleUtil.getCurrentLocale(getActivity()).getISO3Country();

    TelephonyManager tm =
        (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
    String countryCode = tm.getNetworkCountryIso();

    if (response == null) {
      getPresenter().signUpWithPassword(bundle.getFullname(), bundle.getUsername(),
          bundle.getEmail(), bundle.getPassword(), bundle.getBirthdayAsDate(), countryCode, locale,
          selectedTypeIds);
    } else {
      getPresenter().signUpWithProvider(response, bundle.getUsername(), bundle.getBirthdayAsDate(),
          countryCode, locale, selectedTypeIds);
    }
  }

  @Override
  public void onError(Throwable t) {
    // TODO: 18.04.2017 Check error messages
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

  @Override
  public void onShowLoading() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getActivity());
      progressDialog.setMessage(loadingString);
      progressDialog.setIndeterminate(true);
    }

    progressDialog.show();
  }

  @Override
  public void onHideLoading() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override
  public void onSignedUp() {
    getRouter().pushController(RouterTransaction
        .with(RecommendUserController.create())
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }

  @NonNull
  @Override
  public SelectTypePresenter createPresenter() {
    return new SelectTypePresenter(UserRepositoryProvider.getRepository(),
        NotificationRepositoryProvider.getRepository());
  }
}
