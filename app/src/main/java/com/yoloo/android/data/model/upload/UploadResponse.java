package com.yoloo.android.data.model.upload;

import java.util.ArrayList;
import java.util.List;

public class UploadResponse {

  private List<Item> items = new ArrayList<>(3);
  private String nextPageToken;

  public List<Item> getItems() {
    return items;
  }

  public String getNextPageToken() {
    return nextPageToken;
  }

  public static class Item {
    String id;
    String ownerId;
    String mime;
    List<MediaSize> sizes = new ArrayList<>(5);

    public String getId() {
      return id;
    }

    public String getOwnerId() {
      return ownerId;
    }

    public String getMime() {
      return mime;
    }

    public List<MediaSize> getSizes() {
      return sizes;
    }
  }

  public static class MediaSize {
    String url;
    int width;
    int height;

    public String getUrl() {
      return url;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }
  }
}
