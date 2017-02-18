package com.yoloo.android.data;

public class Response<T> {

  private static final long STALE_MS = 5 * 1000; // Data is stale after 5 seconds

  private final T data;
  private final String cursor;
  private final String eTag;
  private final long timestamp;

  private Response(T data, String cursor, String eTag) {
    this.data = data;
    this.cursor = cursor;
    this.eTag = eTag;
    this.timestamp = System.currentTimeMillis();
  }

  public static <T> Response<T> create(T data, String cursor, String eTag) {
    return new Response<>(data, cursor, eTag);
  }

  public T getData() {
    return data;
  }

  public String getCursor() {
    return cursor;
  }

  public String geteTag() {
    return eTag;
  }

  public boolean isUpToDate() {
    return System.currentTimeMillis() - timestamp < STALE_MS;
  }
}
