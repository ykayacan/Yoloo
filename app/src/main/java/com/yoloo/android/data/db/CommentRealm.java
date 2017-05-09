package com.yoloo.android.data.db;

import com.yoloo.backend.yolooApi.model.CommentDTO;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Date;

public class CommentRealm extends RealmObject {

  @PrimaryKey private String id;
  private String ownerId;
  private String username;
  private String avatarUrl;
  @Index private String postId;
  private String content;
  @Index private Date created;
  private int voteDir;
  @Index private boolean accepted;
  private long voteCount;
  private boolean owner;
  private boolean postAccepted;
  private boolean postOwner;
  private int postType;

  public CommentRealm() {
    // empty constructor
  }

  public CommentRealm(CommentRealm comment) {
    this.ownerId = comment.ownerId;
    this.username = comment.username;
    this.avatarUrl = comment.avatarUrl;
    this.postId = comment.postId;
    this.content = comment.content;
    this.created = comment.created;
    this.voteDir = comment.voteDir;
    this.accepted = comment.accepted;
    this.voteCount = comment.voteCount;
    this.owner = comment.owner;
    this.postAccepted = comment.postAccepted;
    this.postOwner = comment.postOwner;
    this.postType = comment.postType;
  }

  public CommentRealm(CommentDTO dto) {
    id = dto.getId();
    ownerId = dto.getOwnerId();
    username = dto.getUsername();
    avatarUrl = dto.getAvatarUrl();
    postId = dto.getPostId();
    content = dto.getContent();
    created = new Date(dto.getCreated().getValue());
    voteDir = dto.getDirection();
    accepted = dto.getAccepted();
    voteCount = dto.getVoteCount();
  }

  public String getId() {
    return id;
  }

  public CommentRealm setId(String id) {
    this.id = id;
    return this;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public CommentRealm setOwnerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public CommentRealm setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public CommentRealm setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  public String getPostId() {
    return postId;
  }

  public CommentRealm setPostId(String postId) {
    this.postId = postId;
    return this;
  }

  public String getContent() {
    return content;
  }

  public CommentRealm setContent(String content) {
    this.content = content;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public CommentRealm setCreated(Date created) {
    this.created = created;
    return this;
  }

  public int getVoteDir() {
    return voteDir;
  }

  public CommentRealm setVoteDir(int voteDir) {
    this.voteDir = voteDir;
    return this;
  }

  public boolean isAccepted() {
    return accepted;
  }

  public CommentRealm setAccepted(boolean accepted) {
    this.accepted = accepted;
    return this;
  }

  public long getVoteCount() {
    return voteCount;
  }

  public CommentRealm setVoteCount(long voteCount) {
    this.voteCount = voteCount;
    return this;
  }

  public boolean isOwner() {
    return owner;
  }

  public CommentRealm setOwner(boolean owner) {
    this.owner = owner;
    return this;
  }

  public boolean isPostAccepted() {
    return postAccepted;
  }

  public CommentRealm setPostAccepted(boolean postAccepted) {
    this.postAccepted = postAccepted;
    return this;
  }

  public boolean isPostOwner() {
    return postOwner;
  }

  public CommentRealm setPostOwner(boolean postOwner) {
    this.postOwner = postOwner;
    return this;
  }

  public int getPostType() {
    return postType;
  }

  public CommentRealm setPostType(int postType) {
    this.postType = postType;
    return this;
  }

  public void increaseVotes() {
    ++this.voteCount;
  }

  public void decreaseVotes() {
    --this.voteCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommentRealm)) return false;

    CommentRealm that = (CommentRealm) o;

    if (getVoteDir() != that.getVoteDir()) return false;
    if (isAccepted() != that.isAccepted()) return false;
    if (getVoteCount() != that.getVoteCount()) return false;
    if (isOwner() != that.isOwner()) return false;
    if (isPostAccepted() != that.isPostAccepted()) return false;
    if (isPostOwner() != that.isPostOwner()) return false;
    if (getPostType() != that.getPostType()) return false;
    if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
    if (getOwnerId() != null
        ? !getOwnerId().equals(that.getOwnerId())
        : that.getOwnerId() != null) {
      return false;
    }
    if (getUsername() != null
        ? !getUsername().equals(that.getUsername())
        : that.getUsername() != null) {
      return false;
    }
    if (getAvatarUrl() != null
        ? !getAvatarUrl().equals(that.getAvatarUrl())
        : that.getAvatarUrl() != null) {
      return false;
    }
    if (getPostId() != null ? !getPostId().equals(that.getPostId()) : that.getPostId() != null) {
      return false;
    }
    if (getContent() != null
        ? !getContent().equals(that.getContent())
        : that.getContent() != null) {
      return false;
    }
    return getCreated() != null
        ? getCreated().equals(that.getCreated())
        : that.getCreated() == null;
  }

  @Override
  public int hashCode() {
    int result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + (getOwnerId() != null ? getOwnerId().hashCode() : 0);
    result = 31 * result + (getUsername() != null ? getUsername().hashCode() : 0);
    result = 31 * result + (getAvatarUrl() != null ? getAvatarUrl().hashCode() : 0);
    result = 31 * result + (getPostId() != null ? getPostId().hashCode() : 0);
    result = 31 * result + (getContent() != null ? getContent().hashCode() : 0);
    result = 31 * result + (getCreated() != null ? getCreated().hashCode() : 0);
    result = 31 * result + getVoteDir();
    result = 31 * result + (isAccepted() ? 1 : 0);
    result = 31 * result + (int) (getVoteCount() ^ (getVoteCount() >>> 32));
    result = 31 * result + (isOwner() ? 1 : 0);
    result = 31 * result + (isPostAccepted() ? 1 : 0);
    result = 31 * result + (isPostOwner() ? 1 : 0);
    result = 31 * result + getPostType();
    return result;
  }

  @Override
  public String toString() {
    return "CommentRealm{"
        + "id='"
        + id
        + '\''
        + ", ownerId='"
        + ownerId
        + '\''
        + ", username='"
        + username
        + '\''
        + ", avatarUrl='"
        + avatarUrl
        + '\''
        + ", postId='"
        + postId
        + '\''
        + ", content='"
        + content
        + '\''
        + ", created="
        + created
        + ", voteDir="
        + voteDir
        + ", accepted="
        + accepted
        + ", voteCount="
        + voteCount
        + ", owner="
        + owner
        + ", postAccepted="
        + postAccepted
        + ", postOwner="
        + postOwner
        + ", postType="
        + postType
        + '}';
  }
}
