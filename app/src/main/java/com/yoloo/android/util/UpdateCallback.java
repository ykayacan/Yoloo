package com.yoloo.android.util;

import io.realm.RealmObject;

public interface UpdateCallback<T extends RealmObject> {
  void onModelUpdated(T item);
}
