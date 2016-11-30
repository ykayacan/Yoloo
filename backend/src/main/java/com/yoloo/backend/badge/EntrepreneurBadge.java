package com.yoloo.backend.badge;

import com.googlecode.objectify.annotation.Subclass;

@Subclass
public class EntrepreneurBadge extends Badge {

    @Override
    public String getName() {
        return "Entrepreneur";
    }

    @Override
    public String getImageUrl() {
        return "https://storage.googleapis.com/yoloo-app.appspot.com/badge/entrepreneur.webp";
    }

    @Override
    public String getContent() {
        return "May the answers be with you !";
    }
}
