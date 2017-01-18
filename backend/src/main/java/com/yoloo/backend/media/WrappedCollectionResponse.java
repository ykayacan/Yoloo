package com.yoloo.backend.media;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Collection;

public class WrappedCollectionResponse<T> extends CollectionResponse<T> {

    private WrappedCollectionResponse(Builder<T> builder) {
        super(builder.items, builder.nextPageToken);
    }

    public static <T> WrappedCollectionResponse.Builder<T> builder() {
        return new WrappedCollectionResponse.Builder<>();
    }

    @Override
    @JsonIgnore
    public String getNextPageToken() {
        return super.getNextPageToken();
    }

    public static class Builder<T> extends CollectionResponse.Builder<T> {
        private Collection<T> items;
        private String nextPageToken;

        @Override
        public WrappedCollectionResponse.Builder<T> setItems(Collection<T> items) {
            this.items = items;
            return this;
        }

        @Override
        public WrappedCollectionResponse.Builder<T> setNextPageToken(String nextPageToken) {
            this.nextPageToken = nextPageToken;
            return this;
        }

        public WrappedCollectionResponse<T> build() {
            return new WrappedCollectionResponse<>(this);
        }
    }
}
