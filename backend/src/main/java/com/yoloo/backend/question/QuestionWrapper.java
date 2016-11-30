package com.yoloo.backend.question;

import javax.servlet.http.HttpServletRequest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class QuestionWrapper {

    private String websafeQuestionId;
    private String content;
    private String hashTags;
    private String mediaId;
    private String categoryIds;
    private int bounty;
    private HttpServletRequest request;
}
