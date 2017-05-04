package com.yoloo.android.feature.auth.signupprogress;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.rafakob.floatingedittext.FloatingEditText;
import com.yoloo.android.R;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.InfoBundle;
import com.yoloo.android.feature.auth.selecttype.SelectTypeController;
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
import io.reactivex.subjects.PublishSubject;
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
  private CompositeDisposable disposable;

  public SignUpProgressController(Bundle args) {
    super(args);
  }

  public static SignUpProgressController create(@LayoutRes int layoutRes, InfoBundle infoBundle) {
    final Bundle bundle = new BundleBuilder()
        .putInt(KEY_LAYOUT_RES, layoutRes)
        .putParcelable(KEY_INFO_BUNDLE, infoBundle)
        .build();

    return new SignUpProgressController(bundle);
  }

  private static Observable<String> getAsObservable(@NonNull final EditText editText) {

    final PublishSubject<String> subject = PublishSubject.create();

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        subject.onNext(s.toString());
      }
    });

    return subject;
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    layoutRes = getArgs().getInt(KEY_LAYOUT_RES);
    return inflater.inflate(layoutRes, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    tvContinue.setVisibility(View.GONE);
    disposable = new CompositeDisposable();

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

  @Override
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  private void setNameAndSurname() {
    Observable<String> nameObservable = getAsObservable(fetName.getEditText());
    Observable<String> surnameObservable = getAsObservable(fetSurname.getEditText());

    Disposable d = Observable
        .combineLatest(nameObservable, surnameObservable,
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
    Observable<String> emailObservable = getAsObservable(fetEmail.getEditText());

    UserRepository repository = UserRepositoryProvider.getRepository();

    Disposable d = emailObservable
        .debounce(400, TimeUnit.MILLISECONDS)
        .map(this::validateEmail)
        .switchMapSingle(result -> {
          if (!result.isValid()) {
            return Single.just(result);
          }

          return repository
              .checkEmail(result.getData())
              .map(available -> available
                  ? ValidationResult.success(result.getData())
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
    Observable<String> usernameObservable = getAsObservable(fetUsername.getEditText());

    UserRepository repository = UserRepositoryProvider.getRepository();

    Disposable d = usernameObservable
        .debounce(400, TimeUnit.MILLISECONDS)
        .map(this::validateUsername)
        .switchMapSingle(result -> {
          if (!result.isValid()) {
            return Single.just(result);
          }

          return repository
              .checkUsername(result.getData())
              .map(available -> available
                  ? ValidationResult.success(result.getData())
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
    Observable<String> passwordObservable = getAsObservable(fetPassword.getEditText());

    Disposable d = passwordObservable
        .debounce(200, TimeUnit.MILLISECONDS)
        .map(this::validatePassword)
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
    final Calendar c = Calendar.getInstance();

    fetBirthday.getEditText().setOnClickListener(v -> {
      DatePickerDialog datePickerDialog =
          new DatePickerDialog(getActivity(), (view, year, month, dayOfMonth) -> {
            fetBirthday.setText(dayOfMonth + "/" + month + "/" + year);
            tvContinue.setVisibility(View.VISIBLE);
          }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
      datePickerDialog.show();
    });

    tvContinue.setOnClickListener(v -> {
      InfoBundle bundle = getArgs().getParcelable(KEY_INFO_BUNDLE);

      InfoBundle newBundle =
          new InfoBundle(bundle.getName(), bundle.getSurname(), bundle.getEmail(),
              bundle.getUsername(), bundle.getPassword(), c.getTimeInMillis());

      startTransaction(SelectTypeController.createWithEmail(newBundle),
          new HorizontalChangeHandler());
    });
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
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

  private ValidationResult<String> validateEmail(@NonNull String email) {
    return ValidationUtils.isValidEmailAddress(email);
  }

  private ValidationResult<String> validateUsername(@NonNull String username) {
    return ValidationUtils.isValidUsername(username);
  }

  private ValidationResult<String> validatePassword(@NonNull String password) {
    return ValidationUtils.isValidPassword(password);
  }
}
