package com.yoloo.android.localmesaagemanager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

@SuppressWarnings({"WeakerAccess", "unused"})
@AnyThread
public class LocalMessageManager implements Callback {

  @NonNull
  private static final String TAG = "LocalMessageManager";

  @Nullable
  private static volatile LocalMessageManager sInstance = null;

  @NonNull
  private final Handler mHandler;

  @NonNull
  private final SparseArray<List<LocalMessageCallback>> mListenersSpecific;

  @NonNull
  private final List<LocalMessageCallback> mListenersUniversal;

  @NonNull
  private final List<LocalMessageCallback> mDefensiveCopyList = new ArrayList<>();

  @NonNull
  private LocalMessage mMessage;

  private LocalMessageManager() {
    mHandler = new Handler(Looper.getMainLooper(), this);
    mMessage = new LocalMessage(null);
    mListenersUniversal = new ArrayList<>();
    mListenersSpecific = new SparseArray<>();
  }

  @NonNull public static LocalMessageManager getInstance() {
    if (sInstance == null) {
      synchronized (LocalMessageManager.class) {
        if (sInstance == null) {
          sInstance = new LocalMessageManager();
        }
      }
    }
    //noinspection ConstantConditions
    return sInstance;
  }

  /**
   * Sends an empty Message containing only the message ID.
   *
   * @param id - message ID
   */
  public final void send(final int id) {
    mHandler.sendEmptyMessage(id);
  }

  /**
   * Sends a Message containing the ID and an arbitrary Object.
   *
   * @param id - message ID
   * @param payload - arbitrary object
   */
  public final void send(final int id, @NonNull final Object payload) {
    mHandler.sendMessage(mHandler.obtainMessage(id, payload));
  }

  /**
   * Sends a Message containing the ID and one integer argument.
   * More effective than sending an arbitrary object.
   *
   * @param id - message ID
   * @param arg1 - integer argument
   */
  public final void send(final int id, final int arg1) {
    mHandler.sendMessage(mHandler.obtainMessage(id, arg1, 0));
  }

  /**
   * Sends a Message containing the ID and two integer arguments.
   * More effective than sending an arbitrary object.
   *
   * @param id - message ID
   * @param arg1 - integer argument
   * @param arg2 - integer argument
   */
  public final void send(final int id, final int arg1, final int arg2) {
    mHandler.sendMessage(mHandler.obtainMessage(id, arg1, arg2));
  }

  /**
   * Sends a Message containing the ID and a Bundle object.
   * More effective than sending an arbitrary object.
   *
   * @param id - message ID
   * @param bundle - bundle
   */
  public final void send(final int id, @NonNull final Bundle bundle) {
    mHandler.sendMessage(mHandler.obtainMessage(id, bundle));
  }

  /**
   * Add listener for specific type of message by its ID.
   * Don't forget to call {@link #removeListener(LocalMessageCallback)} or
   * {@link #removeListeners(int)}
   *
   * @param id ID of message that will be only notified to listener
   * @param listener listener
   */
  public void addListener(int id, @NonNull final LocalMessageCallback listener) {
    synchronized (mListenersSpecific) {
      List<LocalMessageCallback> whatListOfListeners = mListenersSpecific.get(id);
      if (whatListOfListeners == null) {
        whatListOfListeners = new ArrayList<>();
        mListenersSpecific.put(id, whatListOfListeners);
      }
      if (!whatListOfListeners.contains(listener)) {
        whatListOfListeners.add(listener);
      }
    }
  }

  /**
   * Add listener for all messages.
   *
   * @param listener listener
   */
  public void addListener(@NonNull final LocalMessageCallback listener) {
    synchronized (mListenersUniversal) {
      if (!mListenersUniversal.contains(listener)) {
        mListenersUniversal.add(listener);
      } else {
        Timber.w("Listener is already added. %s", listener.toString());
      }
    }
  }

  /**
   * Remove listener for all messages.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(@NonNull final LocalMessageCallback listener) {
    synchronized (mListenersUniversal) {
      if (mListenersUniversal.contains(listener)) {
        mListenersUniversal.remove(listener);
      } else {
        Timber.w("Trying to remove a listener that is not registered. %s", listener.toString());
      }
    }
  }

  /**
   * Remove all listeners for desired message ID.
   *
   * @param id The id of the message to stop listening to.
   */
  public void removeListeners(final int id) {
    final List<LocalMessageCallback> callbacks = mListenersSpecific.get(id);
    if (callbacks == null || callbacks.size() == 0) {
      Timber.w("Trying to remove specific listeners that are not registered. ID %s", id);
    }

    synchronized (mListenersSpecific) {
      mListenersSpecific.delete(id);
    }
  }

  /**
   * Remove the specific listener for desired message ID.
   *
   * @param id The id of the message to stop listening to.
   * @param listener The listener which should be removed.
   */
  public void removeListener(final int id, @NonNull final LocalMessageCallback listener) {
    synchronized (mListenersSpecific) {
      final List<LocalMessageCallback> callbacks = this.mListenersSpecific.get(id);
      if (callbacks != null && !callbacks.isEmpty()) {
        if (callbacks.contains(listener)) {
          callbacks.remove(listener);
        }
      } else {
        Timber.w("Trying to remove specific listener that is not registerred. ID %s", id + ", "
            + listener);
      }
    }
  }

  /*
   * @see android.os.Handler.Callback#handleMessage(android.os.Message)
   */
  @Override public boolean handleMessage(@NonNull final Message msg) {
    mMessage.setMessage(msg);

    logMessageHandling(mMessage);

    // process listeners for specified type of message what
    synchronized (mListenersSpecific) {
      final List<LocalMessageCallback> whatListOfListeners = mListenersSpecific.get(msg.what);
      if (whatListOfListeners != null) {
        if (whatListOfListeners.size() == 0) {
          mListenersSpecific.remove(msg.what);
        } else {
          mDefensiveCopyList.clear();
          mDefensiveCopyList.addAll(whatListOfListeners);
          for (final LocalMessageCallback callback : mDefensiveCopyList) {
            callback.handleMessage(mMessage);
          }
        }
      }
    }

    // process universal listeners
    synchronized (mListenersUniversal) {
      mDefensiveCopyList.clear();
      mDefensiveCopyList.addAll(mListenersUniversal);
      for (final LocalMessageCallback callback : mDefensiveCopyList) {
        callback.handleMessage(mMessage);
      }
    }

    mMessage.setMessage(null);

    return true;
  }

  private void logMessageHandling(@NonNull final LocalMessage msg) {

    final List<LocalMessageCallback> whatListOfListeners = mListenersSpecific.get(msg.getId());

    if ((whatListOfListeners == null || whatListOfListeners.size() == 0)
        && mListenersUniversal.size() == 0) {
      Timber.w("Delivering FAILED for message ID %s. No listeners. %s", msg.getId(),
          msg.toString());
    } else {
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Delivering message ID ");
      stringBuilder.append(msg.getId());
      stringBuilder.append(", Specific listeners: ");
      if (whatListOfListeners == null || whatListOfListeners.size() == 0) {
        stringBuilder.append(0);
      } else {
        stringBuilder.append(whatListOfListeners.size());
        stringBuilder.append(" [");
        for (int i = 0; i < whatListOfListeners.size(); i++) {
          stringBuilder.append(whatListOfListeners.get(i).getClass().getSimpleName());
          if (i < whatListOfListeners.size() - 1) {
            stringBuilder.append(",");
          }
        }
        stringBuilder.append("]");
      }

      stringBuilder.append(", Universal listeners: ");
      synchronized (mListenersUniversal) {
        if (mListenersUniversal.size() == 0) {
          stringBuilder.append(0);
        } else {
          stringBuilder.append(mListenersUniversal.size());
          stringBuilder.append(" [");
          for (int i = 0; i < mListenersUniversal.size(); i++) {
            stringBuilder.append(mListenersUniversal.get(i).getClass().getSimpleName());
            if (i < mListenersUniversal.size() - 1) {
              stringBuilder.append(",");
            }
          }
          stringBuilder.append("], Message: ");
        }
      }
      stringBuilder.append(msg.toString());

      Timber.v(stringBuilder.toString());
    }
  }
}
