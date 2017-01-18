package com.yoloo.backend.util;

import com.yoloo.backend.base.Controller;

public interface ControllerFactory<C extends Controller> {

  C create();
}
