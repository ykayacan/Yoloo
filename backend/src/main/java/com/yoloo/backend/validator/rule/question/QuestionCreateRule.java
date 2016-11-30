package com.yoloo.backend.validator.rule.question;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.backend.validator.Rule;

public class QuestionCreateRule implements Rule<BadRequestException> {

    private static final String LAT_LNG_PATTERN =
            "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$";

    private final String content;
    private final String hashtags;

    public QuestionCreateRule(String content, String hashtags) {
        this.content = content;
        this.hashtags = hashtags;
    }

    @Override
    public void validate() throws BadRequestException {
        /*if (Strings.isNullOrEmpty(content)) {
            throw new BadRequestException("Content can not be empty.");
        }*/
        if (Strings.isNullOrEmpty(hashtags)) {
            throw new BadRequestException("Hashtag cannot be empty.");
        }
        /*if (!Pattern.compile(LAT_LNG_PATTERN).matcher(latLng).matches()) {
            throw new BadRequestException("Invalid latitude or longitude value.");
        }*/
    }
}
