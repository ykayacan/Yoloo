package com.yoloo.android.data.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Date;

public class CommentRealm extends RealmObject {

  @PrimaryKey
  private String id;
  private String ownerId;
  private String username;
  private String avatarUrl;
  @Index
  private String postId;
  private String content;
  @Index
  private Date created;
  private int dir;
  @Index
  private boolean accepted;
  private long votes;

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

  public int getDir() {
    return dir;
  }

  public CommentRealm setDir(int dir) {
    this.dir = dir;
    return this;
  }

  public boolean isAccepted() {
    return accepted;
  }

  public CommentRealm setAccepted(boolean accepted) {
    this.accepted = accepted;
    return this;
  }

  public long getVotes() {
    return votes;
  }

  public CommentRealm setVotes(long votes) {
    this.votes = votes;
    return this;
  }

  public void increaseVotes() {
    ++this.votes;
  }

  public void decreaseVotes() {
    --this.votes;
  }
}
