package com.yoloo.backend.badge;

public abstract class Badge {

    public abstract String getName();

    public abstract String getImageUrl();

    public abstract String getContent();

    @Override
    public boolean equals(Object o) {
        return getName().equals(((Badge) o).getName());
    }
}