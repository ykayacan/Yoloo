/*
 */

package com.yoloo.backend.util;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Gives us our custom version rather than the standard Objectify one.
 *
 * @author Jeff Schnitzer
 */
public class TestObjectifyService extends ObjectifyService {
  public static void initialize() {
    ObjectifyService.setFactory(new TestObjectifyFactory());
  }

  /**
   * @return our extension to Objectify
   */
  public static TestObjectify ofy() {
    return (TestObjectify) ObjectifyService.ofy();
  }

  /**
   * @return our extension to ObjectifyFactory
   */
  public static TestObjectifyFactory fact() {
    return (TestObjectifyFactory) ObjectifyService.factory();
  }

  /**
   * Get a DatastoreService
   */
  public static DatastoreService ds() {
    return DatastoreServiceFactory.getDatastoreService();
  }
}