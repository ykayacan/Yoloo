package com.yoloo.android.data;

public class Response<T> {

  private final T data;
  private final String cursor;
  private final String eTag;

  private Response(T data, String cursor, String eTag) {
    this.data = data;
    this.cursor = cursor;
    this.eTag = eTag;
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
}