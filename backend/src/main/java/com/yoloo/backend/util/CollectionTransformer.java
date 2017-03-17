package com.yoloo.backend.util;

import com.google.api.server.spi.response.CollectionResponse;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.util.Collection;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class CollectionTransformer<U>
    implements ObservableTransformer<Collection<U>, CollectionResponse<U>> {

  private final String cursor;

  @Override
  public ObservableSource<CollectionResponse<U>> apply(Observable<Collection<U>> upstream) {
    return upstream.map(us ->
        CollectionResponse.<U>builder()
            .setItems(us)
            .setNextPageToken(cursor)
            .build());
  }
}
