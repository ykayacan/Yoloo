package com.yoloo.android.data.model;

import com.yoloo.android.backend.modal.yolooApi.model.Account;
import io.reactivex.Observable;
import io.realm.RealmList;
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
  private String provider;
  private String locale;
  private String gender;

  private RealmList<CategoryRealm> categories;

  private long followings;
  private long followers;
  private long questions;

  private int level;
  private int points;
  private int bounties;

  @Index
  private boolean recent;
  private boolean pendingChanges;

  @Ignore
  private String categoryIds;

  @Ignore
  private String password;

  public AccountRealm() {
  }

  public AccountRealm(Account account) {
    this.id = account.getId();
    this.username = account.getUsername();
    this.realname = account.getRealname();
    this.avatarUrl = account.getAvatarUrl().getValue();
    this.email = account.getEmail().getEmail();
    this.me = true;
    this.locale = account.getLocale();
    this.followers = account.getCounts().getFollowers();
    this.followings = account.getCounts().getFollowings();
    this.questions = account.getCounts().getQuestions();
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

  public String getProvider() {
    return provider;
  }

  public AccountRealm setProvider(String provider) {
    this.provider = provider;
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

  public RealmList<CategoryRealm> getCategories() {
    return categories;
  }

  public AccountRealm setCategories(RealmList<CategoryRealm> categories) {
    this.categories = categories;
    return this;
  }

  public long getFollowings() {
    return followings;
  }

  public AccountRealm setFollowings(long followings) {
    this.followings = followings;
    return this;
  }

  public long getFollowers() {
    return followers;
  }

  public AccountRealm setFollowers(long followers) {
    this.followers = followers;
    return this;
  }

  public long getQuestions() {
    return questions;
  }

  public AccountRealm setQuestions(long questions) {
    this.questions = questions;
    return this;
  }

  public int getLevel() {
    return level;
  }

  public AccountRealm setLevel(int level) {
    this.level = level;
    return this;
  }

  public int getPoints() {
    return points;
  }

  public AccountRealm setPoints(int points) {
    this.points = points;
    return this;
  }

  public int getBounties() {
    return bounties;
  }

  public AccountRealm setBounties(int bounties) {
    this.bounties = bounties;
    return this;
  }

  public boolean isPendingChanges() {
    return pendingChanges;
  }

  public AccountRealm setPendingChanges(boolean pendingChanges) {
    this.pendingChanges = pendingChanges;
    return this;
  }

  public AccountRealm setCategoryIds(String categoryIds) {
    this.categoryIds = categoryIds;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public AccountRealm setPassword(String password) {
    this.password = password;
    return this;
  }

  public String getCategoryIds() {
    return Observable.fromIterable(categories)
        .map(CategoryRealm::getName)
        .reduce((s, s2) -> s + "," + s2)
        .map(s -> s.substring(0, s.length() - 1))
        .blockingGet();
  }

  public String toMention() {
    return "@" + username;
  }

  public AccountRealm decreaseBounties(int bounties) {
    this.bounties -= bounties;
    return this;
  }
}