package com.yoloo.android.data;

public final class Response<T> {

  private static final long STALE_MS = 5 * 1000; // Data is stale after 5 seconds

  private final T data;
  private final String cursor;
  private final long timestamp;

  private Response(T data, String cursor) {
    this.data = data;
    this.cursor = cursor;
    this.timestamp = System.currentTimeMillis();
  }

  public static <T> Response<T> create(T data, String cursor) {
    return new Response<>(data, cursor);
  }

  public T getData() {
    return data;
  }

  public String getCursor() {
    return cursor;
  }

  public boolean isUpToDate() {
    return System.currentTimeMillis() - timestamp < STALE_MS;
  }
}
