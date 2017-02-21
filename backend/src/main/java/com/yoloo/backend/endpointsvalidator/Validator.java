package com.yoloo.backend.endpointsvalidator;

import com.google.api.server.spi.ServiceException;

public interface Validator {

  boolean valid();

  void onException() throws ServiceException;
}
