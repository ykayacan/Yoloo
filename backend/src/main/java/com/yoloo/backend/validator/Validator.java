package com.yoloo.backend.validator;

import com.google.api.client.util.Preconditions;
import com.google.api.server.spi.ServiceException;

import java.util.ArrayList;
import java.util.List;

public class Validator {

    private List<Rule> rules = new ArrayList<>(0);

    public Validator(Builder builder) throws ServiceException {
        this.rules = builder.rules;
        validateRules();
    }

    public static Validator.Builder builder() {
        return new Validator.Builder();
    }

    private void validateRules() throws ServiceException {
        Preconditions.checkNotNull(this.rules, "Rule is empty.");
        for (Rule rule : rules) {
            rule.validate();
        }
    }

    public static final class Builder {
        private List<Rule> rules = new ArrayList<>(0);

        public Builder addRule(Rule rule) {
            this.rules.add(rule);
            return this;
        }

        public Builder addRules(List<Rule> rules) {
            this.rules.addAll(rules);
            return this;
        }

        public Validator validate() throws ServiceException {
            return new Validator(this);
        }
    }
}
