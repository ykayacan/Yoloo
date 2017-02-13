package com.yoloo.backend.badge;

import com.googlecode.objectify.annotation.Subclass;

@Subclass
public class FirstUserBadge extends Badge {

    @Override
    public String getName() {
        return "The very first of us";
    }

    @Override
    public String getImageUrl() {
        return "https://storage.googleapis.com/yoloo-app.appspot.com/badge/first_user.webp";
    }

    @Override
    public String getContent() {
        return "You may ask why the hell did i getPost this ?" +
                "Let us explain : you are one of the travelers who joins us in just 30 days." +
                "So, here is a backpack for you to start the adventure !";
    }
}
