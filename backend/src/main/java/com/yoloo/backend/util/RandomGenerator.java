package com.yoloo.backend.util;

import java.util.UUID;

public enum RandomGenerator {
    INSTANCE;

    public String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 25);
    }
}
