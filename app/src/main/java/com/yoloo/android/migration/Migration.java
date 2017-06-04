package com.yoloo.android.migration;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import timber.log.Timber;

public final class Migration implements RealmMigration {

  @Override public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
    Timber.d("Old version: %s", oldVersion);
    Timber.d("New version: %s", newVersion);

    RealmSchema schema = realm.getSchema();

    if (oldVersion == 2) {
      RealmObjectSchema postSchema = schema.get("PostRealm");

      postSchema.removeField("feedItem").removeField("reportCount");
      oldVersion++;
    }
  }
}
