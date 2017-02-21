package com.yoloo.backend.account.condition;

import com.googlecode.objectify.condition.ValueIf;
import com.yoloo.backend.Constants;

public class IfNotAdmin extends ValueIf<String> {
  @Override public boolean matchesValue(String value) {
    return !value.equals(Constants.ADMIN_USERNAME);
  }
}
