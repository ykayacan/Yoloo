package com.yoloo.android.data.util;

import android.os.Parcel;
import android.os.Parcelable;
import io.realm.RealmList;
import io.realm.RealmObject;
import org.parceler.Parcels;
import org.parceler.TypeRangeParcelConverter;

public class RealmListParcelConverter<T extends RealmObject> implements
    TypeRangeParcelConverter<RealmList<? extends RealmObject>, RealmList<T>> {

  private static final int NULL = -1;

  @Override public void toParcel(RealmList<? extends RealmObject> input, Parcel parcel) {
    parcel.writeInt(input == null ? NULL : input.size());
    if (input != null) {
      for (RealmObject item : input) {
        parcel.writeParcelable(Parcels.wrap(item), 0);
      }
    }
  }

  @Override public RealmList<T> fromParcel(Parcel parcel) {
    int size = parcel.readInt();
    RealmList<T> list = new RealmList<>();
    for (int i = 0; i < size; i++) {
      Parcelable parcelable = parcel.readParcelable(getClass().getClassLoader());
      list.add(Parcels.unwrap(parcelable));
    }
    return list;
  }
}
