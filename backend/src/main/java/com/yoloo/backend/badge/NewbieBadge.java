package com.yoloo.backend.badge;

import com.googlecode.objectify.annotation.Subclass;

@Subclass
public class NewbieBadge extends Badge {

    @Override
    public String getName() {
        return "Newbie";
    }

    @Override
    public String getImageUrl() {
        return "https://storage.googleapis.com/yoloo-app.appspot.com/badge/newbie.webp";
    }

    @Override
    public String getContent() {
        return "Don't be sad being just a newbie traveler." +
                "Everybody starts like this. Show your power to us and you'll be surprised !";
    }
}
