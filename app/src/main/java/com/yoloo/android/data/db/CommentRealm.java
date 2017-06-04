package com.yoloo.android.data.db;

import com.yoloo.android.util.Objects;
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

  public boolean showAcceptButton() {
    return !owner && postOwner && !postAccepted && postType != PostRealm.TYPE_BLOG;
  }

  public CommentRealm processPostData(PostRealm post) {
    return setPostType(post.getPostType())
        .setPostAccepted(post.getAcceptedCommentId() != null)
        .setPostOwner(post.isOwner());
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommentRealm that = (CommentRealm) o;
    return voteDir == that.voteDir
        && accepted == that.accepted
        && voteCount == that.voteCount
        && owner == that.owner
        && postAccepted == that.postAccepted
        && postOwner == that.postOwner
        && postType == that.postType
        && Objects.equal(id, that.id)
        && Objects.equal(ownerId, that.ownerId)
        && Objects.equal(username, that.username)
        && Objects.equal(avatarUrl, that.avatarUrl)
        && Objects.equal(postId, that.postId)
        && Objects.equal(content, that.content)
        && Objects.equal(created, that.created);
  }

  @Override public int hashCode() {
    return Objects.hashCode(id, ownerId, username, avatarUrl, postId, content, created, voteDir,
        accepted, voteCount, owner, postAccepted, postOwner, postType);
  }

  @Override public String toString() {
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
