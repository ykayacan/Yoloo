package com.yoloo.backend.base;

public interface ControllerFactory<C extends Controller> {

  C create();
}
