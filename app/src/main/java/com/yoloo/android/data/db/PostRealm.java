package com.yoloo.android.data.db;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.data.util.RealmListParcelConverter;
import com.yoloo.android.util.Objects;
import com.yoloo.backend.yolooApi.model.Post;
import io.realm.PostRealmRealmProxy;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

@Parcel(
    implementations = { PostRealmRealmProxy.class },
    value = Parcel.Serialization.FIELD,
    analyze = { PostRealm.class })
public class PostRealm extends RealmObject {

  public static final int TYPE_TEXT = 0;
  public static final int TYPE_RICH = 1;
  public static final int TYPE_BLOG = 2;
  public static final int TYPE_PHOTO = 3;

  @PrimaryKey String id;

  @Index String ownerId;

  boolean owner;

  String avatarUrl;

  String username;

  String content;

  int bounty;

  @ParcelPropertyConverter(RealmListParcelConverter.class) RealmList<MediaRealm> medias;

  @ParcelPropertyConverter(RealmListParcelConverter.class) RealmList<TagRealm> tags;

  String groupId;

  @Index String acceptedCommentId;

  @Index Date created;

  int voteDir;

  long voteCount;

  long commentCount;

  String title;

  double rank;

  @Index boolean pending;

  @Index boolean bookmarked;

  @Index int postType;

  public PostRealm() {
    medias = new RealmList<>();
    tags = new RealmList<>();
  }

  public PostRealm(Post dto) {
    this();
    id = dto.getId();
    ownerId = dto.getOwnerId();
    owner = dto.getOwner();
    avatarUrl = dto.getAvatarUrl();
    username = dto.getUsername();
    content = dto.getContent();
    bounty = dto.getBounty();
    if (dto.getMedias() != null) {
      List<MediaRealm> medias = Stream.of(dto.getMedias()).map(MediaRealm::new).toList();
      this.medias = new RealmList<>(medias.toArray(new MediaRealm[medias.size()]));
    }
    if (dto.getTags() != null) {
      List<TagRealm> tags = Stream.of(dto.getTags()).map(TagRealm::new).toList();
      this.tags = new RealmList<>(tags.toArray(new TagRealm[tags.size()]));
    }
    acceptedCommentId = dto.getAcceptedCommentId();
    created = new Date(dto.getCreated().getValue());
    voteDir = dto.getDirection();
    voteCount = dto.getVoteCount();
    commentCount = dto.getCommentCount();
    title = dto.getTitle();
    rank = dto.getRank();
    groupId = dto.getGroup();
    postType = dto.getPostType();
    bookmarked = dto.getBookmarked();
  }

  public PostRealm(PostRealm other) {
    id = other.getId();
    ownerId = other.getOwnerId();
    owner = other.isOwner();
    avatarUrl = other.getAvatarUrl();
    username = other.getUsername();
    content = other.getContent();
    bounty = other.getBounty();
    medias = other.getMedias();
    tags = other.getTags();
    groupId = other.getGroupId();
    acceptedCommentId = other.getAcceptedCommentId();
    created = other.getCreated();
    voteDir = other.getVoteDir();
    voteCount = other.getVoteCount();
    commentCount = other.getCommentCount();
    title = other.getTitle();
    rank = other.getRank();
    pending = other.isPending();
    bookmarked = other.isBookmarked();
    postType = other.getPostType();
  }

  @Nullable public FeedItem<?> mapToFeedItem() {
    if (isTextPost()) {
      return new TextPostFeedItem(this);
    } else if (isRichPost()) {
      return new RichPostFeedItem(this);
    } else if (isBlogPost()) {
      return new BlogPostFeedItem(this);
    } else {
      return null;
    }
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

  public RealmList<MediaRealm> getMedias() {
    return medias;
  }

  public PostRealm addMedia(MediaRealm media) {
    this.medias.add(media);
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

  public String getGroupId() {
    return groupId;
  }

  public PostRealm setGroupId(String groupId) {
    this.groupId = groupId;
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

  public String getTagNamesAsString() {
    return Stream.of(tags).map(TagRealm::getName).collect(Collectors.joining(","));
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

  public boolean isOwner() {
    return owner;
  }

  public void increaseVoteCount() {
    ++this.voteCount;
  }

  public void decreaseVoteCount() {
    --this.voteCount;
  }

  public void increaseCommentCount() {
    ++this.commentCount;
  }

  public void decreaseCommentCount() {
    --this.commentCount;
  }

  public boolean isTextPost() {
    return postType == TYPE_TEXT;
  }

  public boolean isRichPost() {
    return postType == TYPE_RICH;
  }

  public boolean isBlogPost() {
    return postType == TYPE_BLOG;
  }

  public boolean isPhotoPost() {
    return postType == TYPE_PHOTO;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PostRealm postRealm = (PostRealm) o;
    return owner == postRealm.owner &&
        bounty == postRealm.bounty &&
        voteDir == postRealm.voteDir &&
        voteCount == postRealm.voteCount &&
        commentCount == postRealm.commentCount &&
        Double.compare(postRealm.rank, rank) == 0 &&
        pending == postRealm.pending &&
        bookmarked == postRealm.bookmarked &&
        postType == postRealm.postType &&
        Objects.equal(id, postRealm.id) &&
        Objects.equal(ownerId, postRealm.ownerId) &&
        Objects.equal(avatarUrl, postRealm.avatarUrl) &&
        Objects.equal(username, postRealm.username) &&
        Objects.equal(content, postRealm.content) &&
        Objects.equal(medias, postRealm.medias) &&
        Objects.equal(tags, postRealm.tags) &&
        Objects.equal(groupId, postRealm.groupId) &&
        Objects.equal(acceptedCommentId, postRealm.acceptedCommentId) &&
        Objects.equal(created, postRealm.created) &&
        Objects.equal(title, postRealm.title);
  }

  @Override public int hashCode() {
    return Objects.hashCode(id, ownerId, owner, avatarUrl, username, content, bounty, medias, tags,
        groupId, acceptedCommentId, created, voteDir, voteCount, commentCount, title, rank, pending,
        bookmarked, postType);
  }

  @Override public String toString() {
    return "PostRealm{" +
        "id='" + id + '\'' +
        ", ownerId='" + ownerId + '\'' +
        ", owner=" + owner +
        ", avatarUrl='" + avatarUrl + '\'' +
        ", username='" + username + '\'' +
        ", content='" + content + '\'' +
        ", bounty=" + bounty +
        ", medias=" + medias +
        ", tags=" + tags +
        ", groupId='" + groupId + '\'' +
        ", acceptedCommentId='" + acceptedCommentId + '\'' +
        ", created=" + created +
        ", voteDir=" + voteDir +
        ", voteCount=" + voteCount +
        ", commentCount=" + commentCount +
        ", title='" + title + '\'' +
        ", rank=" + rank +
        ", pending=" + pending +
        ", bookmarked=" + bookmarked +
        ", postType=" + postType +
        '}';
  }
}
