package com.yoloo.android.util;

import android.support.v4.util.ArrayMap;
import com.bluelinelabs.conductor.Controller;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import timber.log.Timber;

public final class RxBus {

  private static RxBus instance;

  private ArrayMap<Class<?>, BehaviorSubject<BusEvent>> subjects = new ArrayMap<>();

  public static RxBus get() {
    if (instance == null) {
      instance = new RxBus();
    }
    return instance;
  }

  public final void sendEvent(BusEvent event, Class<? extends Controller> target) {
    Subject<BusEvent> subject = getSubject(target);
    subject.onNext(event);
  }

  public Observable<BusEvent> observeEvents(Class<? extends Controller> current) {
    return getSubject(current).doOnDispose(() -> {
      subjects.remove(current);
      Timber.d("Remaining: %s", subjects.size());
    });
  }

  private BehaviorSubject<BusEvent> getSubject(Class<?> target) {
    if (subjects.containsKey(target)) {
      return subjects.get(target);
    } else {
      BehaviorSubject<BusEvent> subject = BehaviorSubject.create();
      subjects.put(target, subject);
      return subject;
    }
  }

  public void clear() {
    subjects.clear();
  }

  public interface BusEvent {
  }
}
