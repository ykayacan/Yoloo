package com.yoloo.android.data.db;

import com.yoloo.backend.yolooApi.model.CountryDTO;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CountryRealm extends RealmObject {
  private @PrimaryKey String code;
  private String name;
  private String flagUrl;

  public CountryRealm() {
  }

  public CountryRealm(CountryDTO dto) {
    code = dto.getCountryCode();
    name = dto.getCountryName();
    flagUrl = dto.getFlagUrl();
  }

  public CountryRealm(String countryCode) {
    code = countryCode;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getFlagUrl() {
    return flagUrl;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CountryRealm)) return false;

    CountryRealm that = (CountryRealm) o;

    if (!getCode().equals(that.getCode())) return false;
    if (!getName().equals(that.getName())) return false;
    return getFlagUrl().equals(that.getFlagUrl());
  }

  @Override public int hashCode() {
    int result = getCode().hashCode();
    result = 31 * result + getName().hashCode();
    result = 31 * result + getFlagUrl().hashCode();
    return result;
  }

  @Override public String toString() {
    return "CountryRealm{" +
        "code='" + code + '\'' +
        ", name='" + name + '\'' +
        ", flagUrl='" + flagUrl + '\'' +
        '}';
  }
}
