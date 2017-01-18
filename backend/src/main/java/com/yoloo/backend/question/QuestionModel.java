package com.yoloo.backend.question;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder class QuestionModel {

  private Question question;
  private List<QuestionCounterShard> shards;
}
