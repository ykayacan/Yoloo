/**
 * Copyright 2016 Ezhome Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy from the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yoloo.android.rxfirebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

/**
 * The class is used as Decorator to
 * Firebase Database functionality with RxJava
 */
public class RxFirebaseDatabase {

  public static volatile RxFirebaseDatabase instance;

  private RxFirebaseDatabase() {
  }

  /**
   * Singleton pattern
   *
   * @return {@link RxFirebaseDatabase}
   */
  public static RxFirebaseDatabase getInstance() {
    if (instance == null) {
      synchronized (RxFirebaseDatabase.class) {
        if (instance == null) {
          instance = new RxFirebaseDatabase();
        }
      }
    }
    return instance;
  }

  /**
   * This methods observes data saving with push in order to generateAll the key
   * automatically according to Firebase hashing key rules.
   *
   * @param firebaseRef {@link Query} this is reference from a Firebase Query
   * @param object {@link Object} whatever object we want to save
   * @return an {@link Observable} from the generated key after the object persistence
   */
  public Observable<String> observeSetValuePush(final DatabaseReference firebaseRef,
      final Object object) {
    return Observable.create(e -> {
      final DatabaseReference ref = firebaseRef.push();

      ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override public void onDataChange(DataSnapshot dataSnapshot) {
          e.onNext(ref.getKey());
          e.onComplete();
        }

        @Override public void onCancelled(DatabaseError error) {
          FirebaseDatabaseErrorFactory.buildError(e, error);
        }
      });
      ref.setValue(object);
    });
  }

  /**
   * This methods observes a firebase query and returns back
   * an Observable from the {@link DataSnapshot}
   * when the firebase client uses a {@link ValueEventListener}
   *
   * @param firebaseRef {@link Query} this is reference from a Firebase Query
   * @return an {@link Observable<DataSnapshot>} from datasnapshot to use
   */
  public Observable<DataSnapshot> observeValueEvent(final Query firebaseRef) {
    return Observable.create(e -> {
      final ValueEventListener listener =
          firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
              e.onNext(dataSnapshot);
            }

            @Override public void onCancelled(DatabaseError error) {
              FirebaseDatabaseErrorFactory.buildError(e, error);
            }
          });

      // When the subscription is cancelled, remove the listener
      e.setCancellable(() -> firebaseRef.removeEventListener(listener));
    });
  }

  /**
   * This methods observes a firebase query and returns back ONCE
   * an Observable from the {@link DataSnapshot}
   * when the firebase client uses a {@link ValueEventListener}
   *
   * @param firebaseRef {@link Query} this is reference from a Firebase Query
   * @return an {@link Observable} from datasnapshot to use
   */
  public Observable<DataSnapshot> observeSingleValue(final Query firebaseRef) {
    return Observable.create(e -> {
      final ValueEventListener listener = new ValueEventListener() {
        @Override public void onDataChange(DataSnapshot dataSnapshot) {
          e.onNext(dataSnapshot);
          e.onComplete();
        }

        @Override public void onCancelled(DatabaseError error) {
          FirebaseDatabaseErrorFactory.buildError(e, error);
        }
      };

      firebaseRef.addListenerForSingleValueEvent(listener);

      // When the subscription is cancelled, remove the listener
      e.setCancellable(() -> firebaseRef.removeEventListener(listener));
    });
  }

  /**
   * This methods observes a firebase query and returns back
   * an Observable from the {@link DataSnapshot}
   * when the firebase client uses a {@link ChildEventListener}
   *
   * @param firebaseRef {@link Query} this is reference from a Firebase Query
   * @return an {@link Observable} from {@link FirebaseChildEvent} to use
   */
  public Observable<FirebaseChildEvent> observeChildEvent(final Query firebaseRef) {
    return Observable.create(e -> {
      final ChildEventListener childEventListener =
          firebaseRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
              e.onNext(new FirebaseChildEvent(dataSnapshot, previousChildName,
                  FirebaseChildEvent.EventType.ADDED));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
              e.onNext(new FirebaseChildEvent(dataSnapshot, previousChildName,
                  FirebaseChildEvent.EventType.CHANGED));
            }

            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
              e.onNext(new FirebaseChildEvent(dataSnapshot, FirebaseChildEvent.EventType.REMOVED));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
              e.onNext(new FirebaseChildEvent(dataSnapshot, previousChildName,
                  FirebaseChildEvent.EventType.MOVED));
            }

            @Override public void onCancelled(DatabaseError error) {
              FirebaseDatabaseErrorFactory.buildError(e, error);
            }
          });

      // This is used to remove the listener when the subscriber is cancelled (unsubscribe)
      // When the subscription is cancelled, remove the listener
      e.setCancellable(() -> firebaseRef.removeEventListener(childEventListener));
    });
  }

  /**
   * Creates an observable only for the child changed method
   *
   * @param firebaseRef {@link Query} this is reference from a Firebase Query
   * @return an {@link Observable} from {@link FirebaseChildEvent} to use
   */
  public Observable<FirebaseChildEvent> observeChildAdded(final Query firebaseRef) {
    return observeChildEvent(firebaseRef).filter(
        filterChildEvent(FirebaseChildEvent.EventType.ADDED));
  }

  /**
   * Creates an observable only for the child changed method
   *
   * @param firebaseRef {@link Query} this is reference from a Firebase Query
   * @return an {@link Observable} from {@link FirebaseChildEvent} to use
   */
  public Observable<FirebaseChildEvent> observeChildChanged(final Query firebaseRef) {
    return observeChildEvent(firebaseRef).filter(
        filterChildEvent(FirebaseChildEvent.EventType.CHANGED));
  }

  /**
   * Creates an observable only for the child removed method
   *
   * @param firebaseRef {@link Query} this is reference from a Firebase Query
   * @return an {@link Observable} from {@link FirebaseChildEvent} to use
   */
  public Observable<FirebaseChildEvent> observeChildRemoved(final Query firebaseRef) {
    return observeChildEvent(firebaseRef).filter(
        filterChildEvent(FirebaseChildEvent.EventType.REMOVED));
  }

  /**
   * Creates an observable only for the child removed method
   *
   * @param firebaseRef {@link Query} this is reference from a Firebase Query
   * @return an {@link Observable} from {@link FirebaseChildEvent} to use
   */
  public Observable<FirebaseChildEvent> observeChildMoved(final Query firebaseRef) {
    return observeChildEvent(firebaseRef).filter(
        filterChildEvent(FirebaseChildEvent.EventType.MOVED));
  }

  /**
   * Functions which filters a stream from {@link Observable} according to firebase
   * child event type
   *
   * @param type {@link FirebaseChildEvent}
   * @return {@link Predicate} a function which returns a boolean if the type are equals
   */
  private Predicate<FirebaseChildEvent> filterChildEvent(final FirebaseChildEvent.EventType type) {
    return event -> event.getEventType() == type;
  }
}
