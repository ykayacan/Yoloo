package com.yoloo.android.data.model;

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

  public CommentRealm() {
    // empty constructor
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
    if (!getId().equals(that.getId())) return false;
    if (!getOwnerId().equals(that.getOwnerId())) return false;
    if (!getUsername().equals(that.getUsername())) return false;
    if (!getAvatarUrl().equals(that.getAvatarUrl())) return false;
    if (!getPostId().equals(that.getPostId())) return false;
    if (!getContent().equals(that.getContent())) return false;
    return getCreated().equals(that.getCreated());
  }

  @Override
  public int hashCode() {
    int result = getId().hashCode();
    result = 31 * result + getOwnerId().hashCode();
    result = 31 * result + getUsername().hashCode();
    result = 31 * result + getAvatarUrl().hashCode();
    result = 31 * result + getPostId().hashCode();
    result = 31 * result + getContent().hashCode();
    result = 31 * result + getCreated().hashCode();
    result = 31 * result + getVoteDir();
    result = 31 * result + (isAccepted() ? 1 : 0);
    result = 31 * result + (int) (getVoteCount() ^ (getVoteCount() >>> 32));
    result = 31 * result + (isOwner() ? 1 : 0);
    result = 31 * result + (isPostAccepted() ? 1 : 0);
    result = 31 * result + (isPostOwner() ? 1 : 0);
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
        + '}';
  }
}
