package com.yoloo.android.rxfirebase;

import com.google.firebase.database.DatabaseError;
import com.yoloo.android.rxfirebase.exception.FirebaseExpiredTokenException;
import com.yoloo.android.rxfirebase.exception.FirebaseGeneralException;
import com.yoloo.android.rxfirebase.exception.FirebaseInvalidTokenException;
import com.yoloo.android.rxfirebase.exception.FirebaseNetworkErrorException;
import com.yoloo.android.rxfirebase.exception.FirebaseOperationFailedException;
import com.yoloo.android.rxfirebase.exception.FirebasePermissionDeniedException;
import io.reactivex.ObservableEmitter;

public class FirebaseDatabaseErrorFactory {

  private FirebaseDatabaseErrorFactory() {
    //empty constructor prevent initialisation
  }

  /**
   * This method add to subsriber the proper error according to the
   *
   * @param e {@link ObservableEmitter}
   * @param error {@link DatabaseError}
   * @param <T> generic subscriber
   */
  static <T> void buildError(ObservableEmitter<T> e, DatabaseError error) {
    switch (error.getCode()) {
      case DatabaseError.INVALID_TOKEN:
        e.onError(new FirebaseInvalidTokenException(error.getMessage()));
        break;
      case DatabaseError.EXPIRED_TOKEN:
        e.onError(new FirebaseExpiredTokenException(error.getMessage()));
        break;
      case DatabaseError.NETWORK_ERROR:
        e.onError(new FirebaseNetworkErrorException(error.getMessage()));
        break;
      case DatabaseError.PERMISSION_DENIED:
        e.onError(new FirebasePermissionDeniedException(error.getMessage()));
        break;
      case DatabaseError.OPERATION_FAILED:
        e.onError(new FirebaseOperationFailedException(error.getMessage()));
        break;
      default:
        e.onError(new FirebaseGeneralException(error.getMessage()));
        break;
    }
  }
}
