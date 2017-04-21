package com.yoloo.backend.travelertype;

import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.common.base.Strings;
import com.googlecode.objectify.Key;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.util.StringUtil;
import io.reactivex.Single;
import ix.Ix;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
@AllArgsConstructor(staticName = "create")
public class TravelerTypeController extends Controller {

  private ImagesService imagesService;

  /**
   * Insert traveler type traveler type.
   *
   * @param displayName the display name
   * @param imageName the image name
   * @param groupIds the group ids
   * @return the traveler type
   */
  public TravelerTypeEntity insertTravelerType(@Nonnull String displayName,
      @Nonnull String imageName, @Nonnull String groupIds) {
    final Key<TravelerTypeEntity> typeKey = TravelerTypeEntity.createKey(displayName);

    final String imageServingUrl;

    if (imageName.equals("dev")) {
      imageServingUrl = Strings.nullToEmpty(imageName);
    } else {
      ServingUrlOptions options = ServingUrlOptions.Builder.withGoogleStorageFileName(
          MediaConfig.SERVE_TRAVELER_TYPES_BUCKET + "/" + imageName.toLowerCase() + ".webp");

      imageServingUrl = imagesService.getServingUrl(options);
    }

    Iterable<String> groupIdsIterable = StringUtil.splitToIterable(groupIds, ",");

    List<Key<TravelerGroupEntity>> groupKeys =
        Ix.from(groupIdsIterable).map(Key::<TravelerGroupEntity>create).toList();

    TravelerTypeEntity travelerTypeEntity = TravelerTypeEntity
        .builder()
        .id(typeKey.getName())
        .name(displayName)
        .imageUrl(new Link(imageServingUrl))
        .groupKeys(groupKeys)
        .build();

    return ofy().transact(() -> {
      ofy().save().entity(travelerTypeEntity).now();

      return travelerTypeEntity;
    });
  }

  /**
   * Update traveler type traveler type entity.
   *
   * @param travelerTypeId the traveler type id
   * @param displayName the display name
   * @param imageName the image name
   * @return the traveler type entity
   */
  public TravelerTypeEntity updateTravelerType(@Nonnull String travelerTypeId,
      @Nullable String displayName, @Nullable String imageName) {
    return Single
        .just(ofy().load().key(Key.<TravelerTypeEntity>create(travelerTypeId)).now())
        .map(entity -> displayName == null ? entity : entity.withName(displayName))
        .map(entity -> imageName == null ? entity : entity.withImageUrl(new Link(imageName)))
        .doOnSuccess(entity -> ofy().save().entity(entity).now())
        .blockingGet();
  }

  /**
   * List traveler types list.
   *
   * @return the list
   */
  public List<TravelerTypeEntity> listTravelerTypes() {
    return ofy().load().type(TravelerTypeEntity.class).list();
  }
}
