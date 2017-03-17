package com.yoloo.android.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.annimon.stream.Stream;
import com.yoloo.backend.yolooApi.model.PostDTO;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class PostRealm extends RealmObject implements Parcelable {

  public static final int POST_TEXT = 0;
  public static final int POST_RICH = 1;
  public static final int POST_BLOG = 2;

  public static final Creator<PostRealm> CREATOR = new Creator<PostRealm>() {
    @Override
    public PostRealm createFromParcel(Parcel in) {
      return new PostRealm(in);
    }

    @Override
    public PostRealm[] newArray(int size) {
      return new PostRealm[size];
    }
  };

  @PrimaryKey
  private String id;
  @Index
  private String ownerId;
  private String avatarUrl;
  private String username;
  private String content;
  private int bounty;
  private String mediaUrl;
  private RealmList<TagRealm> tags;
  private RealmList<CategoryRealm> categories;
  @Index
  private String acceptedCommentId;
  @Index
  private Date created;
  private int voteDir;
  private long voteCount;
  private long commentCount;
  private int reportCount;
  private int postType;
  private String title;
  private double rank;
  @Index
  private boolean isFeedItem;
  @Index
  private boolean pending;
  @Index
  private boolean bookmarked;
  private String categoriesAsString;
  private String tagsAsString;

  public PostRealm() {
    tags = new RealmList<>();
  }

  public PostRealm(PostDTO dto) {
    id = dto.getId();
    ownerId = dto.getOwnerId();
    avatarUrl = dto.getAvatarUrl();
    username = dto.getUsername();
    content = dto.getContent();
    bounty = dto.getBounty();
    if (dto.getMedia() != null) {
      mediaUrl = dto.getMedia().getSizes().get(0).getUrl();
    }
    acceptedCommentId = dto.getAcceptedCommentId();
    created = new Date(dto.getCreated().getValue());
    voteDir = dto.getDirection();
    voteCount = dto.getVoteCount();
    commentCount = dto.getCommentCount();
    reportCount = dto.getReportCount();
    title = dto.getTitle();
    rank = dto.getRank().doubleValue();
  }

  protected PostRealm(Parcel in) {
    id = in.readString();
    ownerId = in.readString();
    avatarUrl = in.readString();
    username = in.readString();
    content = in.readString();
    bounty = in.readInt();
    mediaUrl = in.readString();
    acceptedCommentId = in.readString();
    voteDir = in.readInt();
    voteCount = in.readLong();
    commentCount = in.readLong();
    reportCount = in.readInt();
    postType = in.readInt();
    title = in.readString();
    rank = in.readDouble();
    categoriesAsString = in.readString();
    tagsAsString = in.readString();
    isFeedItem = in.readByte() != 0;
    pending = in.readByte() != 0;
    bookmarked = in.readByte() != 0;
  }

  public String getId() {
    return id;
  }

  public PostRealm setId(String id) {
    this.id = id;
    return this;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public PostRealm setOwnerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public PostRealm setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public PostRealm setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getContent() {
    return content;
  }

  public PostRealm setContent(String content) {
    this.content = content;
    return this;
  }

  public int getBounty() {
    return bounty;
  }

  public PostRealm setBounty(int bounty) {
    this.bounty = bounty;
    return this;
  }

  @Nullable public String getMediaUrl() {
    return mediaUrl;
  }

  public PostRealm setMediaUrl(String mediaUrl) {
    this.mediaUrl = mediaUrl;
    return this;
  }

  public RealmList<TagRealm> getTags() {
    return tags;
  }

  public PostRealm setTags(RealmList<TagRealm> tags) {
    this.tags = tags;
    return this;
  }

  public PostRealm addTag(TagRealm tag) {
    this.tags.add(tag);
    return this;
  }

  public List<String> getTagNames() {
    return Stream.of(tags).map(TagRealm::getName).toList();
  }

  public RealmList<CategoryRealm> getCategories() {
    return categories;
  }

  public PostRealm setCategories(RealmList<CategoryRealm> categories) {
    this.categories = categories;
    return this;
  }

  public List<String> getCategoryNames() {
    return Stream.of(categories).map(CategoryRealm::getName).toList();
  }

  public PostRealm addCategory(CategoryRealm categoryRealm) {
    this.categories.add(categoryRealm);
    return this;
  }

  public boolean isCommented() {
    return commentCount != 0L;
  }

  public String getAcceptedCommentId() {
    return acceptedCommentId;
  }

  public PostRealm setAcceptedCommentId(String acceptedCommentId) {
    this.acceptedCommentId = acceptedCommentId;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public PostRealm setCreated(Date created) {
    this.created = created;
    return this;
  }

  public int getVoteDir() {
    return voteDir;
  }

  public PostRealm setVoteDir(int voteDir) {
    this.voteDir = voteDir;
    return this;
  }

  public long getVoteCount() {
    return voteCount;
  }

  public PostRealm setVoteCount(long voteCount) {
    this.voteCount = voteCount;
    return this;
  }

  public long getCommentCount() {
    return commentCount;
  }

  public PostRealm setCommentCount(long commentCount) {
    this.commentCount = commentCount;
    return this;
  }

  public int getReportCount() {
    return reportCount;
  }

  public PostRealm setReportCount(int reportCount) {
    this.reportCount = reportCount;
    return this;
  }

  public int getPostType() {
    return postType;
  }

  public PostRealm setPostType(int postType) {
    this.postType = postType;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public PostRealm setTitle(String title) {
    this.title = title;
    return this;
  }

  public double getRank() {
    return rank;
  }

  public PostRealm setRank(double rank) {
    this.rank = rank;
    return this;
  }

  public String getCategoriesAsString() {
    return categoriesAsString;
  }

  public PostRealm setCategoriesAsString(String categoriesAsString) {
    this.categoriesAsString = categoriesAsString;
    return this;
  }

  public String getTagsAsString() {
    return tagsAsString;
  }

  public PostRealm setTagsAsString(String tagsAsString) {
    this.tagsAsString = tagsAsString;
    return this;
  }

  public boolean isFeedItem() {
    return isFeedItem;
  }

  public PostRealm setFeedItem(boolean feedItem) {
    isFeedItem = feedItem;
    return this;
  }

  public boolean isPending() {
    return pending;
  }

  public PostRealm setPending(boolean pending) {
    this.pending = pending;
    return this;
  }

  public boolean isBookmarked() {
    return bookmarked;
  }

  public PostRealm setBookmarked(boolean bookmarked) {
    this.bookmarked = bookmarked;
    return this;
  }

  public PostRealm increaseVoteCount() {
    ++this.voteCount;
    return this;
  }

  public PostRealm decreaseVoteCount() {
    --this.voteCount;
    return this;
  }

  public PostRealm increaseCommentCount() {
    ++this.commentCount;
    return this;
  }

  public PostRealm decreaseCommentCount() {
    --this.commentCount;
    return this;
  }

  public boolean shouldShowReadMore() {
    return content.length() >= 200;
  }

  @Override public String toString() {
    return "PostRealm{" +
        "id='" + id + '\'' +
        ", ownerId='" + ownerId + '\'' +
        ", avatarUrl='" + avatarUrl + '\'' +
        ", username='" + username + '\'' +
        ", content='" + content + '\'' +
        ", bounty=" + bounty +
        ", mediaUrl='" + mediaUrl + '\'' +
        ", tags=" + tags +
        ", categories=" + categories +
        ", acceptedCommentId='" + acceptedCommentId + '\'' +
        ", created=" + created +
        ", voteDir=" + voteDir +
        ", voteCount=" + voteCount +
        ", commentCount=" + commentCount +
        ", reportCount=" + reportCount +
        ", postType=" + postType +
        ", title='" + title + '\'' +
        ", rank=" + rank +
        ", isFeedItem=" + isFeedItem +
        ", pending=" + pending +
        ", bookmarked=" + bookmarked +
        ", categoriesAsString='" + categoriesAsString + '\'' +
        ", tagsAsString='" + tagsAsString + '\'' +
        '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(ownerId);
    dest.writeString(avatarUrl);
    dest.writeString(username);
    dest.writeString(content);
    dest.writeInt(bounty);
    dest.writeString(mediaUrl);
    dest.writeString(acceptedCommentId);
    dest.writeInt(voteDir);
    dest.writeLong(voteCount);
    dest.writeLong(commentCount);
    dest.writeInt(reportCount);
    dest.writeInt(postType);
    dest.writeString(title);
    dest.writeDouble(rank);
    dest.writeString(categoriesAsString);
    dest.writeString(tagsAsString);
    dest.writeByte((byte) (isFeedItem ? 1 : 0));
    dest.writeByte((byte) (pending ? 1 : 0));
    dest.writeByte((byte) (bookmarked ? 1 : 0));
  }
}
