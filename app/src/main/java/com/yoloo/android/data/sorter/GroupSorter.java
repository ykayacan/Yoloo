package com.yoloo.android.data.sorter;

public enum GroupSorter {
  TRENDING("trending"),
  DEFAULT("default");

  private String title;

  GroupSorter(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }
}
