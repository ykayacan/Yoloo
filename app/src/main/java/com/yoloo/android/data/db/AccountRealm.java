package com.yoloo.android.data.db;

import android.content.res.Resources;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.backend.yolooApi.model.AccountDTO;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

public class AccountRealm extends RealmObject {

  @PrimaryKey private String id;
  @Index private boolean me;
  private String username;
  private String realname;
  private String email;
  private String avatarUrl;
  private String bio;
  private String gender;
  private String websiteUrl;
  private Date birthdate;
  private String langCode;
  private CountryRealm country;
  private RealmList<CountryRealm> visitedCountries;
  private boolean following;

  private long followingCount;
  private long followerCount;
  private long postCount;

  private int countryCount;
  private String levelTitle;
  private int level;
  private int pointCount;
  private int bountyCount;

  private boolean pending;
  @Index private Date localSaveDate;
  @Index boolean recent;

  private String idToken;
  private String subscribedGroupIds;

  @Ignore private String password;
  @Ignore private List<String> travelerTypeIds;
  @Ignore private String facebookId;

  public AccountRealm() {
    visitedCountries = new RealmList<>();
  }

  public AccountRealm(AccountDTO dto) {
    this();
    id = dto.getId();
    username = dto.getUsername();
    realname = dto.getRealname();
    email = dto.getEmail();
    bio = dto.getBio();
    gender = dto.getGender();
    websiteUrl = dto.getWebsiteUrl();
    avatarUrl = dto.getAvatarUrl();
    langCode = dto.getLangCode();
    country = new CountryRealm(dto.getCountry());

    if (dto.getVisitedCountries() != null) {
      Stream
          .of(dto.getVisitedCountries())
          .map(CountryRealm::new)
          .forEach(country -> visitedCountries.add(country));

      countryCount = visitedCountries.size();
    }

    following = dto.getFollowing();
    followingCount = dto.getFollowingCount();
    followerCount = dto.getFollowerCount();
    postCount = dto.getPostCount();

    levelTitle = dto.getLevelTitle();
    level = dto.getLevel();
    pointCount = dto.getPointCount();
    bountyCount = dto.getBountyCount();
    if (dto.getSubscribedGroupIds() != null) {
      subscribedGroupIds = Stream.of(dto.getSubscribedGroupIds()).collect(Collectors.joining(","));
    }
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

  public String getLangCode() {
    return langCode;
  }

  public AccountRealm setLangCode(String langCode) {
    this.langCode = langCode;
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

  public int getCountryCount() {
    return countryCount;
  }

  public AccountRealm setCountryCount(int countryCount) {
    this.countryCount = countryCount;
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

  @Nullable
  public String getSubscribedGroupIds() {
    return subscribedGroupIds;
  }

  public AccountRealm setSubscribedGroupIds(String subscribedGroupIds) {
    this.subscribedGroupIds = subscribedGroupIds;
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

  public Date getBirthdate() {
    return birthdate;
  }

  public AccountRealm setBirthdate(Date birthdate) {
    this.birthdate = birthdate;
    return this;
  }

  public CountryRealm getCountry() {
    return country;
  }

  public AccountRealm setCountry(CountryRealm country) {
    this.country = country;
    return this;
  }

  public List<CountryRealm> getVisitedCountries() {
    return visitedCountries;
  }

  public AccountRealm addVisitedCountry(CountryRealm country) {
    this.visitedCountries.add(country);
    return this;
  }

  public List<String> getTravelerTypeIds() {
    return travelerTypeIds;
  }

  public AccountRealm setTravelerTypeIds(List<String> travelerTypeIds) {
    this.travelerTypeIds = travelerTypeIds;
    return this;
  }

  public Date getLocalSaveDate() {
    return localSaveDate;
  }

  public AccountRealm setLocalSaveDate(Date localSaveDate) {
    this.localSaveDate = localSaveDate;
    return this;
  }

  public String getFacebookId() {
    return facebookId;
  }

  public AccountRealm setFacebookId(String facebookId) {
    this.facebookId = facebookId;
    return this;
  }

  public String getLevelTitle() {
    Resources res = YolooApp.getAppContext().getResources();

    if (levelTitle == null) {
      return null;
    }

    switch (levelTitle) {
      case "Travel Enthusiast":
        return res.getString(R.string.level_2);
      case "Explorer":
        return res.getString(R.string.level_3);
      case "Traveler":
        return res.getString(R.string.level_4);
      case "Full Time Traveler":
        return res.getString(R.string.level_5);
      default:
        return "";
    }
  }

  public void setLevelTitle(String levelTitle) {
    this.levelTitle = levelTitle;
  }

  public boolean isRecent() {
    return recent;
  }

  public AccountRealm setRecent(boolean recent) {
    this.recent = recent;
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
    if (this == o) return true;
    if (!(o instanceof AccountRealm)) return false;

    AccountRealm that = (AccountRealm) o;

    if (isMe() != that.isMe()) return false;
    if (isFollowing() != that.isFollowing()) return false;
    if (getFollowingCount() != that.getFollowingCount()) return false;
    if (getFollowerCount() != that.getFollowerCount()) return false;
    if (getPostCount() != that.getPostCount()) return false;
    if (getCountryCount() != that.getCountryCount()) return false;
    if (getLevel() != that.getLevel()) return false;
    if (getPointCount() != that.getPointCount()) return false;
    if (getBountyCount() != that.getBountyCount()) return false;
    if (isPending() != that.isPending()) return false;
    if (isRecent() != that.isRecent()) return false;
    if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
    if (getUsername() != null ? !getUsername().equals(that.getUsername())
        : that.getUsername() != null) {
      return false;
    }
    if (getRealname() != null ? !getRealname().equals(that.getRealname())
        : that.getRealname() != null) {
      return false;
    }
    if (getEmail() != null ? !getEmail().equals(that.getEmail()) : that.getEmail() != null) {
      return false;
    }
    if (getAvatarUrl() != null ? !getAvatarUrl().equals(that.getAvatarUrl())
        : that.getAvatarUrl() != null) {
      return false;
    }
    if (getBio() != null ? !getBio().equals(that.getBio()) : that.getBio() != null) return false;
    if (getGender() != null ? !getGender().equals(that.getGender()) : that.getGender() != null) {
      return false;
    }
    if (getWebsiteUrl() != null ? !getWebsiteUrl().equals(that.getWebsiteUrl())
        : that.getWebsiteUrl() != null) {
      return false;
    }
    if (getBirthdate() != null ? !getBirthdate().equals(that.getBirthdate())
        : that.getBirthdate() != null) {
      return false;
    }
    if (getLangCode() != null ? !getLangCode().equals(that.getLangCode())
        : that.getLangCode() != null) {
      return false;
    }
    if (getCountry() != null ? !getCountry().equals(that.getCountry())
        : that.getCountry() != null) {
      return false;
    }
    if (getVisitedCountries() != null ? !getVisitedCountries().equals(that.getVisitedCountries())
        : that.getVisitedCountries() != null) {
      return false;
    }
    if (getLevelTitle() != null ? !getLevelTitle().equals(that.getLevelTitle())
        : that.getLevelTitle() != null) {
      return false;
    }
    if (getLocalSaveDate() != null ? !getLocalSaveDate().equals(that.getLocalSaveDate())
        : that.getLocalSaveDate() != null) {
      return false;
    }
    if (getIdToken() != null ? !getIdToken().equals(that.getIdToken())
        : that.getIdToken() != null) {
      return false;
    }
    if (getSubscribedGroupIds() != null ? !getSubscribedGroupIds().equals(
        that.getSubscribedGroupIds()) : that.getSubscribedGroupIds() != null) {
      return false;
    }
    if (getPassword() != null ? !getPassword().equals(that.getPassword())
        : that.getPassword() != null) {
      return false;
    }
    if (getTravelerTypeIds() != null ? !getTravelerTypeIds().equals(that.getTravelerTypeIds())
        : that.getTravelerTypeIds() != null) {
      return false;
    }
    return getFacebookId() != null ? getFacebookId().equals(that.getFacebookId())
        : that.getFacebookId() == null;
  }

  @Override public int hashCode() {
    int result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + (isMe() ? 1 : 0);
    result = 31 * result + (getUsername() != null ? getUsername().hashCode() : 0);
    result = 31 * result + (getRealname() != null ? getRealname().hashCode() : 0);
    result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
    result = 31 * result + (getAvatarUrl() != null ? getAvatarUrl().hashCode() : 0);
    result = 31 * result + (getBio() != null ? getBio().hashCode() : 0);
    result = 31 * result + (getGender() != null ? getGender().hashCode() : 0);
    result = 31 * result + (getWebsiteUrl() != null ? getWebsiteUrl().hashCode() : 0);
    result = 31 * result + (getBirthdate() != null ? getBirthdate().hashCode() : 0);
    result = 31 * result + (getLangCode() != null ? getLangCode().hashCode() : 0);
    result = 31 * result + (getCountry() != null ? getCountry().hashCode() : 0);
    result = 31 * result + (getVisitedCountries() != null ? getVisitedCountries().hashCode() : 0);
    result = 31 * result + (isFollowing() ? 1 : 0);
    result = 31 * result + (int) (getFollowingCount() ^ (getFollowingCount() >>> 32));
    result = 31 * result + (int) (getFollowerCount() ^ (getFollowerCount() >>> 32));
    result = 31 * result + (int) (getPostCount() ^ (getPostCount() >>> 32));
    result = 31 * result + getCountryCount();
    result = 31 * result + (getLevelTitle() != null ? getLevelTitle().hashCode() : 0);
    result = 31 * result + getLevel();
    result = 31 * result + getPointCount();
    result = 31 * result + getBountyCount();
    result = 31 * result + (isPending() ? 1 : 0);
    result = 31 * result + (getLocalSaveDate() != null ? getLocalSaveDate().hashCode() : 0);
    result = 31 * result + (isRecent() ? 1 : 0);
    result = 31 * result + (getIdToken() != null ? getIdToken().hashCode() : 0);
    result =
        31 * result + (getSubscribedGroupIds() != null ? getSubscribedGroupIds().hashCode() : 0);
    result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
    result = 31 * result + (getTravelerTypeIds() != null ? getTravelerTypeIds().hashCode() : 0);
    result = 31 * result + (getFacebookId() != null ? getFacebookId().hashCode() : 0);
    return result;
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
        ", gender='" + gender + '\'' +
        ", websiteUrl='" + websiteUrl + '\'' +
        ", birthdate=" + birthdate +
        ", langCode='" + langCode + '\'' +
        ", country=" + country +
        ", visitedCountries=" + visitedCountries +
        ", following=" + following +
        ", followingCount=" + followingCount +
        ", followerCount=" + followerCount +
        ", postCount=" + postCount +
        ", countryCount=" + countryCount +
        ", levelTitle='" + levelTitle + '\'' +
        ", level=" + level +
        ", pointCount=" + pointCount +
        ", bountyCount=" + bountyCount +
        ", pending=" + pending +
        ", localSaveDate=" + localSaveDate +
        ", recent=" + recent +
        ", idToken='" + idToken + '\'' +
        ", subscribedGroupIds='" + subscribedGroupIds + '\'' +
        ", password='" + password + '\'' +
        ", travelerTypeIds=" + travelerTypeIds +
        ", facebookId='" + facebookId + '\'' +
        '}';
  }
}
