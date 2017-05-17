package com.yoloo.android.feature.auth.signupprepared;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.rafakob.floatingedittext.FloatingEditText;
import com.yoloo.android.R;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.IdpResponse;
import com.yoloo.android.feature.auth.InfoBundle;
import com.yoloo.android.feature.auth.signupdiscover.SignUpDiscoverController;
import com.yoloo.android.feature.auth.util.ValidationResult;
import com.yoloo.android.feature.auth.util.ValidationUtils;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;

public class SignUpPreparedController extends BaseController {

  private static final String KEY_IDP_RESPONSE = "IDP_RESPONSE";

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.fet_sign_up_fullname) FloatingEditText fetFullname;
  @BindView(R.id.fet_sign_up_email) FloatingEditText fetEmail;
  @BindView(R.id.fet_sign_up_username) FloatingEditText fetUsername;
  //@BindView(R.id.fet_sign_up_birthday) FloatingEditText fetBirthday;
  @BindView(R.id.tv_sign_up_continue) TextView tvContinue;

  private CompositeDisposable disposable = new CompositeDisposable();

  public SignUpPreparedController(Bundle args) {
    super(args);
  }

  public static SignUpPreparedController create(@NonNull IdpResponse idpResponse) {
    final Bundle bundle = new BundleBuilder().putParcelable(KEY_IDP_RESPONSE, idpResponse).build();

    return new SignUpPreparedController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_sign_up_prepared, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);

    //fetBirthday.getEditText().setKeyListener(null);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    final IdpResponse idpResponse = getArgs().getParcelable(KEY_IDP_RESPONSE);

    setViewsWithDefaultInfo(idpResponse);

    UserRepository repository = UserRepositoryProvider.getRepository();

    //final Calendar calendar = Calendar.getInstance();
    //setBirthday(calendar);

    /*Observable<String> birthdayObservable =
        RxTextView.afterTextChangeEvents(fetBirthday.getEditText())
            .map(event -> event.editable().toString());*/

    Disposable d = getUsernameObservable(repository)
        .subscribe(result -> tvContinue.setVisibility(result.isValid() ? View.VISIBLE : View.GONE));

    /*Disposable d = Observable.combineLatest(usernameObservable, birthdayObservable,
        (username, birthday) -> username.isValid() && !TextUtils.isEmpty(birthday))
        .subscribe(enable -> tvContinue.setVisibility(enable ? View.VISIBLE : View.GONE));*/

    disposable.add(d);

    navigateToSignUpDiscoverScreen(idpResponse);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private void setViewsWithDefaultInfo(IdpResponse idpResponse) {
    fetFullname.setText(idpResponse.getName());
    fetFullname.setEnabled(false);
    fetEmail.setText(idpResponse.getEmail());
    fetEmail.setEnabled(false);
  }

  private void navigateToSignUpDiscoverScreen(IdpResponse idpResponse) {
    tvContinue.setOnClickListener(v -> {
      InfoBundle bundle =
          new InfoBundle(idpResponse.getName(), null, idpResponse.getEmail(), fetUsername.getText(),
              null);

      KeyboardUtil.hideKeyboard(getView());
      startTransaction(SignUpDiscoverController.createWithProvider(idpResponse, bundle),
          new HorizontalChangeHandler());
    });
  }

  private Observable<ValidationResult<String>> getUsernameObservable(UserRepository repository) {
    return RxTextView.afterTextChangeEvents(fetUsername.getEditText())
        .debounce(400, TimeUnit.MILLISECONDS)
        .map(event -> ValidationUtils.isValidUsername(event.editable().toString()))
        .switchMapSingle(result -> {
          if (!result.isValid()) {
            return Single.just(result);
          }

          return repository.checkUsername(result.getData())
              .map(available -> available ? ValidationResult.success(result.getData())
                  : ValidationResult.failure("Username is already taken", result.getData()));
        })
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(result -> fetUsername.setError(result.getReason()));
  }

  /*private void setBirthday(Calendar calendar) {
    RxView.clicks(fetBirthday.getEditText()).subscribe(o -> showDatePickerDialog(calendar));
  }*/

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  /*private void showDatePickerDialog(Calendar calendar) {
    DatePickerDialog datePickerDialog =
        new DatePickerDialog(getActivity(), (view, year, month, dayOfMonth) -> {
          month += 1;

          fetBirthday.setText(dayOfMonth + "/" + month + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));

    datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
    datePickerDialog.show();
  }*/
}
