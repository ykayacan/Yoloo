package com.yoloo.android.feature.auth.util;

import android.support.annotation.Nullable;

public class ValidationResult<T> {

  private boolean valid;
  private String reason;
  private T data;

  private ValidationResult(boolean valid, @Nullable String reason, T t) {
    this.valid = valid;
    this.reason = reason;
    this.data = t;
  }

  public static <T> ValidationResult<T> success(T t) {
    return new ValidationResult<>(true, null, t);
  }

  public static <T> ValidationResult<T> failure(@Nullable String reason, T t) {
    return new ValidationResult<>(false, reason, t);
  }

  public boolean isValid() {
    return valid;
  }

  @Nullable
  public String getReason() {
    return reason;
  }

  public T getData() {
    return data;
  }
}
