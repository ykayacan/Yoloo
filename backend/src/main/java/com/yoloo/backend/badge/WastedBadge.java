package com.yoloo.backend.badge;

import com.googlecode.objectify.annotation.Subclass;

@Subclass
public class WastedBadge extends Badge {

    @Override
    public String getName() {
        return "Wasted";
    }

    @Override
    public String getImageUrl() {
        return "https://storage.googleapis.com/yoloo-app.appspot.com/badge/wasted.webp";
    }

    @Override
    public String getContent() {
        return "Hmm… It seems like you are a little bit hurt but that's okey," +
                "ask again and give an another chance !";
    }
}
