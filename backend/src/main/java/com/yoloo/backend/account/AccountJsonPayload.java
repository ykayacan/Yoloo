package com.yoloo.backend.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor
public class AccountJsonPayload {
  private String username;
  private String realname;
  private String email;
  private String countryCode;
  private String langCode;
  private String profileImageUrl;
  private List<String> travelerTypeIds;

  @SneakyThrows({UnsupportedEncodingException.class, IOException.class})
  public static AccountJsonPayload from(@Nonnull String base64Payload) {
    final byte[] decodedBytes = BaseEncoding.base64Url().decode(base64Payload);
    final String payload = new String(decodedBytes, "UTF-8");

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(payload, AccountJsonPayload.class);
  }
}
