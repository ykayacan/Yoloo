package com.yoloo.android.feature.auth.util;

import android.support.annotation.NonNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {

  private static final String MOBILE_NUMBER_REGEX = "[7-9][0-9]{9}$";
  private static final String TEXT_WITH_MOBILE_NUMBER_REGEX = ".*[7-9][0-9]{9}.*";
  private static final String TEXT_WITH_EMAIL_ADDRESS_REGEX =
      ".*[a-zA-Z0-9\\+\\" + ".\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9]{1,64}\\.[a-zA-Z0-9]{1,25}.*";

  private static final String USERNAME_REGEX = "^[a-zA-Z][a-zA-Z._0-9]{2,19}$";
  private static final String TEXT_WITH_FOUR_CONSECUTIVE_NUMBERS_REGEX = ".*[0-9]{5,}.*";

  public static boolean isValidMobileNumber(String number) {
    Pattern mPattern = Pattern.compile(MOBILE_NUMBER_REGEX);
    Matcher matcher = mPattern.matcher(number);
    return matcher.find();
  }

  public static ValidationResult<String> isValidUsername(String username) {
    if (username.isEmpty()) {
      return ValidationResult.failure(null, username);
    }

    if (username.length() < 3) {
      return ValidationResult.failure("username should have 3 or more characters", username);
    }

    Pattern mPattern = Pattern.compile(USERNAME_REGEX);
    Matcher matcher = mPattern.matcher(username);
    boolean isValid = matcher.find();

    if (isValid) {
      return ValidationResult.success(username);
    }

    return ValidationResult.failure("username should contain only alphanumeric characters",
        username);
  }

  public static ValidationResult<String> isValidPassword(String password) {
    if (password.isEmpty()) {
      return ValidationResult.failure(null, password);
    }

    if (password.length() < 6) {
      return ValidationResult.failure("password should have 6 or more characters", password);
    }

    return ValidationResult.success(password);
  }

  public static boolean containsFourConsecutiveNumbers(String text) {
    Pattern mPattern = Pattern.compile(TEXT_WITH_FOUR_CONSECUTIVE_NUMBERS_REGEX);
    Matcher matcher = mPattern.matcher(text);
    return matcher.find();
  }

  public static boolean containsMobileNumber(String text) {
    Pattern mPattern = Pattern.compile(TEXT_WITH_MOBILE_NUMBER_REGEX);
    Matcher matcher = mPattern.matcher(text);
    return matcher.find();
  }

  public static ValidationResult<String> isValidEmailAddress(@NonNull String text) {
    if (text.isEmpty()) {
      return ValidationResult.failure(null, text);
    }

    Pattern mPattern = Pattern.compile(TEXT_WITH_EMAIL_ADDRESS_REGEX);
    Matcher matcher = mPattern.matcher(text);
    boolean isValid = matcher.find();

    if (isValid) {
      return ValidationResult.success(text);
    }

    return ValidationResult.failure("Please enter correct email address", text);
  }
}
