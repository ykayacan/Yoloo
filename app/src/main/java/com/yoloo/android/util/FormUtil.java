package com.yoloo.android.util;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.annimon.stream.Stream;
import com.github.jksiezni.permissive.Permissive;

public final class FormUtil {

  public static boolean isEmailAddress(CharSequence target) {
    return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
  }

  public static boolean isWebUrl(CharSequence target) {
    return Patterns.WEB_URL.matcher(target).matches();
  }

  public static boolean isPasswordValid(CharSequence password) {
    return password.length() > 4;
  }

  public static void populateEmail(Activity activity, AutoCompleteTextView view) {
    new Permissive.Request(Manifest.permission.READ_CONTACTS)
        .whenPermissionsGranted(permissions -> addEmailsToAutoComplete(activity, view))
        .execute(activity);
  }

  private static void addEmailsToAutoComplete(Context context, AutoCompleteTextView view) {
    Account[] deviceAccounts = AccountManager.get(context).getAccounts();

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(context,
            android.R.layout.simple_dropdown_item_1line, Stream.of(deviceAccounts)
            .map(account -> account.name)
            .filter(FormUtil::isEmailAddress)
            .toList());

    view.setAdapter(adapter);
  }
}
