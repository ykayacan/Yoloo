package com.yoloo.backend.badge;

import com.googlecode.objectify.annotation.Subclass;

@Subclass
public class WelcomeBadge extends Badge {

    @Override
    public String getName() {
        return "Welcome";
    }

    @Override
    public String getImageUrl() {
        return "https://storage.googleapis.com/yoloo-app.appspot.com/badge/welcome.webp";
    }

    @Override
    public String getContent() {
        return "Tada..! Welcome to the Backpacker's community !" +
                "As backpackers, we love to share our experiences here and have fun." +
                "So, why are you waiting ? Choose now : ASK or ANSWER ;)";
    }
}
