package com.yoloo.android.feature.feed;

import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.RecommendedGroupListFeedItem;
import java.util.List;

public interface PartialStateChanges {

  /**
   * Indicates that the first page is loading
   */
  final class FirstPageLoading implements PartialStateChanges {

    @Override public String toString() {
      return "FirstPageLoadingState{}";
    }
  }

  /**
   * Indicates that an error has occurred while loading the first page
   */
  final class FirstPageError implements PartialStateChanges {
    private final Throwable error;

    public FirstPageError(Throwable error) {
      this.error = error;
    }

    public Throwable getError() {
      return error;
    }

    @Override public String toString() {
      return "FirstPageErrorState{" +
          "error=" + error +
          '}';
    }
  }

  /**
   * Indicates that the first page data has been loaded successfully
   */
  final class FirstPageLoaded implements PartialStateChanges {
    private final List<FeedItem<?>> data;

    public FirstPageLoaded(List<FeedItem<?>> data) {
      this.data = data;
    }

    public List<FeedItem<?>> getData() {
      return data;
    }
  }

  /**
   * Next Page has been loaded successfully
   */
  final class NextPageLoaded implements PartialStateChanges {
    private final List<FeedItem<?>> data;

    public NextPageLoaded(List<FeedItem<?>> data) {
      this.data = data;
    }

    public List<FeedItem<?>> getData() {
      return data;
    }
  }

  /**
   * Error while loading new page
   */
  final class NexPageLoadingError implements PartialStateChanges {
    private final Throwable error;

    public NexPageLoadingError(Throwable error) {
      this.error = error;
    }

    public Throwable getError() {
      return error;
    }
  }

  /**
   * Indicates that loading the next page has started
   */
  final class NextPageLoading implements PartialStateChanges {
  }

  /**
   * Indicates that loading the newest items via pull to refresh has started
   */
  final class PullToRefreshLoading implements PartialStateChanges {
  }

  /**
   * Indicates that an error while loading the newest items via pull to refresh has occurred
   */
  final class PullToRefreshLoadingError implements PartialStateChanges {
    private final Throwable error;

    public PullToRefreshLoadingError(Throwable error) {
      this.error = error;
    }

    public Throwable getError() {
      return error;
    }
  }

  /**
   * Indicates that data has been loaded successfully over pull-to-refresh
   */
  final class PullToRefreshLoaded implements PartialStateChanges {
    private final List<FeedItem<?>> data;

    public PullToRefreshLoaded(List<FeedItem<?>> data) {
      this.data = data;
    }

    public List<FeedItem<?>> getData() {
      return data;
    }
  }

  /**
   * Loading all Products of a given category has been started
   */
  final class GroupsOfRecommendedGroupsSectionLoading implements PartialStateChanges {
    private final String sectionName;

    public GroupsOfRecommendedGroupsSectionLoading(String sectionName) {
      this.sectionName = sectionName;
    }

    public String getSectionName() {
      return sectionName;
    }
  }

  /**
   * An error while loading all products has been occurred
   */
  final class GroupsOfRecommendedGroupsSectionLoadingError implements PartialStateChanges {
    private final String sectionName;
    private final Throwable error;

    public GroupsOfRecommendedGroupsSectionLoadingError(String sectionName, Throwable error) {
      this.sectionName = sectionName;
      this.error = error;
    }

    public String getSectionName() {
      return sectionName;
    }

    public Throwable getError() {
      return error;
    }
  }

  /**
   * Products of a given Category has been loaded
   */
  final class GroupsOfRecommendedGroupsSectionLoaded implements PartialStateChanges {
    private final RecommendedGroupListFeedItem data;
    private final String sectionName;

    public GroupsOfRecommendedGroupsSectionLoaded(String sectionName,
        RecommendedGroupListFeedItem data) {
      this.data = data;
      this.sectionName = sectionName;
    }

    public String getSectionName() {
      return sectionName;
    }

    public RecommendedGroupListFeedItem getData() {
      return data;
    }
  }
}
