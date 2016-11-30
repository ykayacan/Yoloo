package com.yoloo.backend.saved;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.Question;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
public class SavedQuestionService {

    public SavedQuestion create(Key<Question> questionKey, Key<Account> parentKey) {
        return SavedQuestion.builder()
                .id(questionKey.toWebSafeString())
                .parentUserKey(parentKey)
                .build();
    }
}
