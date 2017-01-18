package com.yoloo.android.data.sorter;

public enum PostSorter {
  NEWEST("newest"),
  HOT("hot"),
  UNANSWERED("unanswered"),
  BOUNTY("bounty");

  private String title;

  PostSorter(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }
}
