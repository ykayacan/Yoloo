package com.yoloo.android.data.sorter;

public enum CategorySorter {
  TRENDING("trending"),
  DEFAULT("default");

  private String title;

  CategorySorter(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }
}
