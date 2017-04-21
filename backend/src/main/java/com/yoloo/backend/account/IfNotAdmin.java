package com.yoloo.backend.account;

import com.googlecode.objectify.condition.ValueIf;
import com.yoloo.backend.Constants;

class IfNotAdmin extends ValueIf<String> {
  @Override public boolean matchesValue(String value) {
    return !value.equals(Constants.ADMIN_USERNAME);
  }
}
