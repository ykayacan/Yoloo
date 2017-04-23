package com.yoloo.backend.media;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = Constants.API_OWNER, ownerName = Constants.API_OWNER))
@ApiClass(resource = "medias", clientIds = {
    Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
}, audiences = {Constants.AUDIENCE_ID}, authenticators = {FirebaseAuthenticator.class})
public class MediaEndpoint {

  /**
   * Get media.
   *
   * @param mediaId the user id
   * @param user the user
   * @return the media
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "medias.get", path = "medias/{mediaId}", httpMethod = ApiMethod.HttpMethod.GET)
  public MediaEntity get(@Named("mediaId") String mediaId, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(mediaId, "mediaId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(mediaId, "Invalid mediaId."));

    return getMediaController().getMedia(mediaId, user);
  }

  /**
   * List collection response.
   *
   * @param userId the user id
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the collection response
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "medias.list", path = "medias/{userId}", httpMethod = ApiMethod.HttpMethod.POST)
  public CollectionResponse<MediaEntity> list(@Named("userId") String userId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(userId, "userId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(userId, "Invalid userId."));

    return getMediaController().listMedias(userId, Optional.fromNullable(limit),
        Optional.fromNullable(cursor), user);
  }

  @ApiMethod(name = "medias.listRecentMedias",
      path = "medias",
      httpMethod = ApiMethod.HttpMethod.POST)
  public CollectionResponse<MediaEntity> listRecentMedia(@Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit, User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return getMediaController().listRecentMedias(Optional.fromNullable(limit),
        Optional.fromNullable(cursor));
  }

  private MediaController getMediaController() {
    return MediaController.create(MediaService.create());
  }
}
