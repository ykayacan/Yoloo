package com.yoloo.android.feature.auth.signupprogress;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SignUpProgressController extends BaseController {

  private static final String KEY_LAYOUT_RES = "LAYOUT_RES";
  private static final String KEY_INFO_BUNDLE = "INFO_BUNDLE";

  @BindView(R.id.toolbar) Toolbar toolbar;

  @Nullable @BindView(R.id.fet_sign_up_name) FloatingEditText fetName;
  @Nullable @BindView(R.id.fet_sign_up_surname) FloatingEditText fetSurname;
  @Nullable @BindView(R.id.fet_sign_up_email) FloatingEditText fetEmail;
  @Nullable @BindView(R.id.fet_sign_up_username) FloatingEditText fetUsername;
  @Nullable @BindView(R.id.fet_sign_up_password) FloatingEditText fetPassword;
  @Nullable @BindView(R.id.fet_sign_up_birthday) FloatingEditText fetBirthday;

  @BindView(R.id.btn_sign_up_continue) TextView tvContinue;

  private @LayoutRes int layoutRes;

  private CompositeDisposable disposable = new CompositeDisposable();
  private UserRepository repository = UserRepositoryProvider.getRepository();

  public SignUpProgressController(Bundle args) {
    super(args);
  }

  public static SignUpProgressController create(@LayoutRes int layoutRes, InfoBundle infoBundle) {
    final Bundle bundle = new BundleBuilder().putInt(KEY_LAYOUT_RES, layoutRes)
        .putParcelable(KEY_INFO_BUNDLE, infoBundle)
        .build();

    return new SignUpProgressController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    layoutRes = getArgs().getInt(KEY_LAYOUT_RES);
    return inflater.inflate(layoutRes, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    tvContinue.setVisibility(View.GONE);

    switch (layoutRes) {
      case R.layout.controller_sign_up_name:
        setNameAndSurname();
        break;
      case R.layout.controller_sign_up_email:
        setEmail();
        break;
      case R.layout.controller_sign_up_username:
        setUsername();
        break;
      case R.layout.controller_sign_up_password:
        setPassword();
        break;
      case R.layout.controller_sign_up_birthday:
        setBirthday();
        break;
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  private void setNameAndSurname() {

    Observable<String> nameObservable = RxTextView.afterTextChangeEvents(fetName.getEditText())
        .map(event -> event.editable().toString());

    Observable<String> surnameObservable =
        RxTextView.afterTextChangeEvents(fetSurname.getEditText())
            .map(event -> event.editable().toString());

    Disposable d = Observable.combineLatest(nameObservable, surnameObservable,
        (s, s2) -> !TextUtils.isEmpty(s) && !TextUtils.isEmpty(s2))
        .subscribe(enable -> tvContinue.setVisibility(enable ? View.VISIBLE : View.GONE));

    disposable.add(d);

    tvContinue.setOnClickListener(v -> {
      InfoBundle bundle =
          new InfoBundle(fetName.getText(), fetSurname.getText(), null, null, null, 0);

      startTransaction(SignUpProgressController.create(R.layout.controller_sign_up_email, bundle),
          new HorizontalChangeHandler());
    });
  }

  private void setEmail() {
    Disposable d = RxTextView.afterTextChangeEvents(fetEmail.getEditText())
        .debounce(400, TimeUnit.MILLISECONDS)
        .map(event -> event.editable().toString())
        .map(ValidationUtils::isValidEmailAddress)
        .switchMapSingle(result -> {
          if (!result.isValid()) {
            return Single.just(result);
          }

          return repository.checkEmail(result.getData())
              .map(available -> available ? ValidationResult.success(result.getData())
                  : ValidationResult.failure("Email is already taken", result.getData()));
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(result -> {
          fetEmail.setError(result.getReason());
          tvContinue.setVisibility(result.isValid() ? View.VISIBLE : View.GONE);

          InfoBundle bundle = getArgs().getParcelable(KEY_INFO_BUNDLE);

          InfoBundle newBundle =
              new InfoBundle(bundle.getName(), bundle.getSurname(), result.getData(), null, null,
                  0);

          tvContinue.setOnClickListener(v -> startTransaction(
              SignUpProgressController.create(R.layout.controller_sign_up_username, newBundle),
              new HorizontalChangeHandler()));
        });

    disposable.add(d);
  }

  private void setUsername() {
    Disposable d = RxTextView.afterTextChangeEvents(fetUsername.getEditText())
        .debounce(400, TimeUnit.MILLISECONDS)
        .map(event -> event.editable().toString())
        .map(ValidationUtils::isValidUsername)
        .switchMapSingle(result -> {
          if (!result.isValid()) {
            return Single.just(result);
          }

          return repository.checkUsername(result.getData())
              .map(available -> available ? ValidationResult.success(result.getData())
                  : ValidationResult.failure("Username is already taken", result.getData()));
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(result -> {
          fetUsername.setError(result.getReason());
          tvContinue.setVisibility(result.isValid() ? View.VISIBLE : View.GONE);

          InfoBundle bundle = getArgs().getParcelable(KEY_INFO_BUNDLE);

          InfoBundle newBundle =
              new InfoBundle(bundle.getName(), bundle.getSurname(), bundle.getEmail(),
                  result.getData(), null, 0);

          tvContinue.setOnClickListener(v -> startTransaction(
              SignUpProgressController.create(R.layout.controller_sign_up_password, newBundle),
              new HorizontalChangeHandler()));
        });

    disposable.add(d);
  }

  private void setPassword() {
    Disposable d = RxTextView.afterTextChangeEvents(fetPassword.getEditText())
        .debounce(200, TimeUnit.MILLISECONDS)
        .map(event -> event.editable().toString())
        .map(ValidationUtils::isValidPassword)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(result -> {
          fetPassword.setError(result.getReason());
          tvContinue.setVisibility(result.isValid() ? View.VISIBLE : View.GONE);

          InfoBundle bundle = getArgs().getParcelable(KEY_INFO_BUNDLE);

          InfoBundle newBundle =
              new InfoBundle(bundle.getName(), bundle.getSurname(), bundle.getEmail(),
                  bundle.getUsername(), result.getData(), 0);

          tvContinue.setOnClickListener(v -> startTransaction(
              SignUpProgressController.create(R.layout.controller_sign_up_birthday, newBundle),
              new HorizontalChangeHandler()));
        });

    disposable.add(d);
  }

  private void setBirthday() {
    fetBirthday.getEditText().setKeyListener(null);

    final Calendar c = Calendar.getInstance();

    fetBirthday.getEditText().setOnClickListener(v -> showDatePickerDialog(c));

    RxTextView.afterTextChangeEvents(fetBirthday.getEditText())
        .map(event -> event.editable().toString())
        .subscribe(s -> tvContinue.setVisibility(s.isEmpty() ? View.GONE : View.VISIBLE));

    tvContinue.setOnClickListener(v -> {
      InfoBundle bundle = getArgs().getParcelable(KEY_INFO_BUNDLE);

      InfoBundle newBundle =
          new InfoBundle(bundle.getName(), bundle.getSurname(), bundle.getEmail(),
              bundle.getUsername(), bundle.getPassword(), c.getTimeInMillis());

      startTransaction(SignUpDiscoverController.createWithEmail(newBundle),
          new HorizontalChangeHandler());
    });
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void startTransaction(Controller to, ControllerChangeHandler handler) {
    getRouter().pushController(
        RouterTransaction.with(to).pushChangeHandler(handler).popChangeHandler(handler));
  }

  private void showDatePickerDialog(Calendar calendar) {
    DatePickerDialog datePickerDialog =
        new DatePickerDialog(getActivity(), (view, year, month, dayOfMonth) -> {
          month += 1;

          fetBirthday.setText(dayOfMonth + "/" + month + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));

    datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
    datePickerDialog.show();
  }
}
