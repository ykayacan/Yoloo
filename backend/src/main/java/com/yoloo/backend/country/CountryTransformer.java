package com.yoloo.backend.country;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.account.dto.AccountDTO;

public class CountryTransformer implements Transformer<Country, AccountDTO.CountryDTO> {

  @Override
  public AccountDTO.CountryDTO transformTo(Country in) {
    return new AccountDTO.CountryDTO(in);
  }

  @Override
  public Country transformFrom(AccountDTO.CountryDTO in) {
    return null;
  }
}
