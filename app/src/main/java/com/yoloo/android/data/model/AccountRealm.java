package com.yoloo.android.data.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yoloo.backend.yolooApi.model.AccountDTO;

import java.util.Date;
import java.util.Objects;

import javax.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class AccountRealm extends RealmObject {

  @PrimaryKey
  private String id;
  @Index
  private boolean me;
  private String username;
  private String realname;
  private String email;
  private String avatarUrl;
  private String bio;
  private String locale;
  private String gender;
  private String websiteUrl;
  private boolean following;

  private long followingCount;
  private long followerCount;
  private long postCount;

  private int achievementCount;
  private int level;
  private int pointCount;
  private int bountyCount;

  @Index
  private boolean recent;
  private boolean pending;
  @Index
  private Date localSaveDate;

  private String idToken;
  private String categoryIds;

  @Ignore
  private String password;

  public AccountRealm() {
    // Empty constructor.
  }

  public AccountRealm(AccountDTO dto) {
    id = dto.getId();
    username = dto.getUsername();
    realname = dto.getRealname();
    email = dto.getEmail();
    bio = dto.getBio();
    gender = dto.getGender();
    websiteUrl = dto.getWebsiteUrl();
    avatarUrl = dto.getAvatarUrl();
    locale = dto.getLocale();
    following = dto.getFollowing();
    followingCount = dto.getFollowingCount();
    followerCount = dto.getFollowerCount();
    postCount = dto.getPostCount();
    level = dto.getLevel();
    pointCount = dto.getPointCount();
    bountyCount = dto.getBountyCount();
    categoryIds = Stream.of(dto.getInterestedCategoryIds()).collect(Collectors.joining(","));
    localSaveDate = new Date();
  }

  public String getId() {
    return id;
  }

  public AccountRealm setId(String id) {
    this.id = id;
    return this;
  }

  public boolean isMe() {
    return me;
  }

  public AccountRealm setMe(boolean me) {
    this.me = me;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public AccountRealm setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getRealname() {
    return realname;
  }

  public AccountRealm setRealname(String realname) {
    this.realname = realname;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public AccountRealm setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public AccountRealm setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  public String getLocale() {
    return locale;
  }

  public AccountRealm setLocale(String locale) {
    this.locale = locale;
    return this;
  }

  public String getGender() {
    return gender;
  }

  public AccountRealm setGender(String gender) {
    this.gender = gender;
    return this;
  }

  public boolean isFollowing() {
    return following;
  }

  public AccountRealm setFollowing(boolean following) {
    this.following = following;
    return this;
  }

  public long getFollowingCount() {
    return followingCount;
  }

  public AccountRealm setFollowingCount(long followingCount) {
    this.followingCount = followingCount;
    return this;
  }

  public long getFollowerCount() {
    return followerCount;
  }

  public AccountRealm setFollowerCount(long followerCount) {
    this.followerCount = followerCount;
    return this;
  }

  public long getPostCount() {
    return postCount;
  }

  public AccountRealm setPostCount(long postCount) {
    this.postCount = postCount;
    return this;
  }

  public int getAchievementCount() {
    return achievementCount;
  }

  public AccountRealm setAchievementCount(int achievementCount) {
    this.achievementCount = achievementCount;
    return this;
  }

  public int getLevel() {
    return level;
  }

  public AccountRealm setLevel(int level) {
    this.level = level;
    return this;
  }

  public int getPointCount() {
    return pointCount;
  }

  public AccountRealm setPointCount(int pointCount) {
    this.pointCount = pointCount;
    return this;
  }

  public int getBountyCount() {
    return bountyCount;
  }

  public AccountRealm setBountyCount(int bountyCount) {
    this.bountyCount = bountyCount;
    return this;
  }

  public boolean isPending() {
    return pending;
  }

  public AccountRealm setPending(boolean pending) {
    this.pending = pending;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public AccountRealm setPassword(String password) {
    this.password = password;
    return this;
  }

  @Nullable public String getCategoryIds() {
    return categoryIds;
  }

  public AccountRealm setCategoryIds(String categoryIds) {
    this.categoryIds = categoryIds;
    return this;
  }

  public String getIdToken() {
    return idToken;
  }

  public AccountRealm setIdToken(String idToken) {
    this.idToken = idToken;
    return this;
  }

  public String getBio() {
    return bio;
  }

  public AccountRealm setBio(String bio) {
    this.bio = bio;
    return this;
  }

  public String getWebsiteUrl() {
    return websiteUrl;
  }

  public AccountRealm setWebsiteUrl(String websiteUrl) {
    this.websiteUrl = websiteUrl;
    return this;
  }

  public Date getLocalSaveDate() {
    return localSaveDate;
  }

  public AccountRealm setLocalSaveDate(Date localSaveDate) {
    this.localSaveDate = localSaveDate;
    return this;
  }

  public String toMention() {
    return "@" + username;
  }

  public AccountRealm decreaseBounties(int bounties) {
    this.bountyCount -= bounties;
    return this;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountRealm that = (AccountRealm) o;
    return me == that.me &&
        following == that.following &&
        followingCount == that.followingCount &&
        followerCount == that.followerCount &&
        postCount == that.postCount &&
        achievementCount == that.achievementCount &&
        level == that.level &&
        pointCount == that.pointCount &&
        bountyCount == that.bountyCount &&
        recent == that.recent &&
        pending == that.pending &&
        Objects.equals(id, that.id) &&
        Objects.equals(username, that.username) &&
        Objects.equals(realname, that.realname) &&
        Objects.equals(email, that.email) &&
        Objects.equals(avatarUrl, that.avatarUrl) &&
        Objects.equals(bio, that.bio) &&
        Objects.equals(locale, that.locale) &&
        Objects.equals(gender, that.gender) &&
        Objects.equals(websiteUrl, that.websiteUrl) &&
        Objects.equals(localSaveDate, that.localSaveDate) &&
        Objects.equals(idToken, that.idToken) &&
        Objects.equals(categoryIds, that.categoryIds);
  }

  @Override public int hashCode() {
    return Objects.hash(id, me, username, realname, email, avatarUrl, bio, locale, gender,
        websiteUrl,
        following, followingCount, followerCount, postCount, achievementCount, level, pointCount,
        bountyCount, recent, pending, localSaveDate, idToken, categoryIds);
  }

  @Override public String toString() {
    return "AccountRealm{" +
        "id='" + id + '\'' +
        ", me=" + me +
        ", username='" + username + '\'' +
        ", realname='" + realname + '\'' +
        ", email='" + email + '\'' +
        ", avatarUrl='" + avatarUrl + '\'' +
        ", bio='" + bio + '\'' +
        ", locale='" + locale + '\'' +
        ", gender='" + gender + '\'' +
        ", websiteUrl='" + websiteUrl + '\'' +
        ", following=" + following +
        ", followingCount=" + followingCount +
        ", followerCount=" + followerCount +
        ", postCount=" + postCount +
        ", achievementCount=" + achievementCount +
        ", level=" + level +
        ", pointCount=" + pointCount +
        ", bountyCount=" + bountyCount +
        ", recent=" + recent +
        ", pending=" + pending +
        ", localSaveDate=" + localSaveDate +
        ", idToken='" + idToken + '\'' +
        ", categoryIds='" + categoryIds + '\'' +
        ", password='" + password + '\'' +
        '}';
  }
}
