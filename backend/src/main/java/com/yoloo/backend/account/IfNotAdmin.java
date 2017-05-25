package com.yoloo.backend.account;

import com.google.common.base.Strings;
import com.googlecode.objectify.condition.ValueIf;
import com.yoloo.backend.Constants;

class IfNotAdmin extends ValueIf<String> {
  @Override public boolean matchesValue(String value) {
    return !Strings.isNullOrEmpty(value) && !value.equals(Constants.ADMIN_USERNAME);
  }
}
