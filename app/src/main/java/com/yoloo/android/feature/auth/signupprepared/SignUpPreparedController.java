package com.yoloo.android.feature.auth.signupprepared;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.yoloo.android.feature.auth.IdpResponse;
import com.yoloo.android.feature.auth.InfoBundle;
import com.yoloo.android.feature.auth.selecttype.SelectTypeController;
import com.yoloo.android.feature.auth.util.ValidationResult;
import com.yoloo.android.feature.auth.util.ValidationUtils;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.util.BundleBuilder;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SignUpPreparedController extends BaseController {

  private static final String KEY_IDP_RESPONSE = "IDP_RESPONSE";

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.fet_sign_up_fullname) FloatingEditText fetFullname;
  @BindView(R.id.fet_sign_up_email) FloatingEditText fetEmail;
  @BindView(R.id.fet_sign_up_username) FloatingEditText fetUsername;
  @BindView(R.id.fet_sign_up_birthday) FloatingEditText fetBirthday;
  @BindView(R.id.tv_sign_up_continue) TextView tvContinue;

  private CompositeDisposable disposable;

  public SignUpPreparedController(Bundle args) {
    super(args);
  }

  public static SignUpPreparedController create(@NonNull IdpResponse idpResponse) {
    final Bundle bundle = new BundleBuilder().putParcelable(KEY_IDP_RESPONSE, idpResponse).build();

    return new SignUpPreparedController(bundle);
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
    return inflater.inflate(R.layout.controller_sign_up_prepared, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    disposable = new CompositeDisposable();

    final IdpResponse idpResponse = getArgs().getParcelable(KEY_IDP_RESPONSE);

    setViewsWithDefaultInfo(idpResponse);

    UserRepository repository = UserRepositoryProvider.getRepository();

    Observable<ValidationResult<String>> usernameObservable = getUsernameObservable(repository);
    Observable<String> birthdayObservable = getAsObservable(fetBirthday.getEditText());

    final Calendar c = Calendar.getInstance();
    setBirthday(c);

    Disposable d = Observable
        .combineLatest(usernameObservable, birthdayObservable,
            (s, s2) -> s.isValid() && !TextUtils.isEmpty(s2))
        .subscribe(enable -> tvContinue.setVisibility(enable ? View.VISIBLE : View.GONE));

    disposable.add(d);

    tvContinue.setOnClickListener(v -> {
      InfoBundle bundle =
          new InfoBundle(idpResponse.getName(), null, idpResponse.getEmail(), fetUsername.getText(),
              null, c.getTimeInMillis());

      startTransaction(SelectTypeController.createWithProvider(idpResponse, bundle),
          new HorizontalChangeHandler());
    });
  }

  private void setViewsWithDefaultInfo(IdpResponse idpResponse) {
    fetFullname.setText(idpResponse.getName());
    fetFullname.setEnabled(false);
    fetEmail.setText(idpResponse.getEmail());
    fetEmail.setEnabled(false);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private Observable<ValidationResult<String>> getUsernameObservable(UserRepository repository) {
    return getAsObservable(fetUsername.getEditText())
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
        .doOnNext(result -> {
          if (!result.isValid()) {
            tvContinue.setVisibility(View.GONE);
          }
          fetUsername.setError(result.getReason());
        });
  }

  private void setBirthday(Calendar c) {
    fetBirthday.getEditText().setOnClickListener(v -> {
      DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
          (view, year, month, dayOfMonth) -> fetBirthday.setText(
              dayOfMonth + "/" + month + "/" + year), c.get(Calendar.YEAR), c.get(Calendar.MONTH),
          c.get(Calendar.DAY_OF_MONTH));
      datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
      datePickerDialog.show();
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

  private ValidationResult<String> validateUsername(@NonNull String username) {
    return ValidationUtils.isValidUsername(username);
  }
}
