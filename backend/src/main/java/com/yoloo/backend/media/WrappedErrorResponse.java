package com.yoloo.backend.media;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WrappedErrorResponse {

    private int code;
    private String message;
}
