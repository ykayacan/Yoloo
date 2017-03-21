package com.yoloo.backend.endpointsvalidator;

import com.google.api.server.spi.ServiceException;

public interface Validator {

  boolean isValid();

  void onException() throws ServiceException;
}
