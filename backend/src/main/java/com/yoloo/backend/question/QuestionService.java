package com.yoloo.backend.question;

import com.google.common.base.Optional;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.vote.Vote;

import org.joda.time.DateTime;

import java.util.List;

import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public class QuestionService {

    public Question create(Account account, QuestionWrapper wrapper, QuestionShardService service) {
        final Key<Question> questionKey =
                ofy().factory().allocateId(account.getKey(), Question.class);

        return Question.builder()
                .id(questionKey.getId())
                .parentUserKey(account.getKey())
                .avatarUrl(account.getAvatarUrl())
                .username(account.getUsername())
                .content(wrapper.getContent())
                .shardKeys(service.getShardKeys(questionKey))
                .hashTags(StringUtil.splitToSet(wrapper.getHashTags(), ","))
                .dir(Vote.Direction.DEFAULT)
                .bounty(wrapper.getBounty())
                .acceptedComment(null)
                .comments(0)
                .votes(0)
                .reports(0)
                .firstComment(false)
                .created(DateTime.now())
                .build();
    }

    public Question update(Question question, QuestionWrapper wrapper) {
        Optional<Integer> bounty = Optional.fromNullable(wrapper.getBounty());
        if (bounty.isPresent()) {
            question = question.withBounty(bounty.get());
        }

        Optional<String> content = Optional.fromNullable(wrapper.getContent());
        if (content.isPresent()) {
            question = question.withContent(content.get());
        }

        Optional<String> hashTags = Optional.fromNullable(wrapper.getHashTags());
        if (hashTags.isPresent()) {
            question = question.withHashTags(StringUtil.splitToSet(hashTags.get(), ","));
        }

        // TODO: 28.11.2016 Implement media update.

        return question;
    }

    public List<Key<Comment>> getCommentKeys(Key<Question> questionKey) {
        return ofy().load().type(Comment.class)
                .filter(Comment.FIELD_QUESTION_KEY + " =", questionKey)
                .keys().list();
    }

    public List<Key<Vote>> getVoteKeys(Key<Question> questionKey) {
        return ofy().load().type(Vote.class)
                .filter(Vote.FIELD_VOTABLE_KEY + " =", questionKey)
                .keys().list();
    }
}
