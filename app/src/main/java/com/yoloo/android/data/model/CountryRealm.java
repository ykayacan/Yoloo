package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.CountryDTO;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CountryRealm extends RealmObject {
  @PrimaryKey private String code;
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
}
