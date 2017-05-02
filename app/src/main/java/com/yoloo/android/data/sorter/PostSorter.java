package com.yoloo.android.data.sorter;

public enum PostSorter {
  NEWEST("newest"),
  HOT("hot"),
  UNANSWERED("unanswered"),
  BOUNTY("bounty");

  private String name;

  PostSorter(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
