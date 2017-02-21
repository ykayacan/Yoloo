package com.yoloo.backend.media;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import com.yoloo.backend.post.Post;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(
    name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER,
        packagePath = Constants.API_PACKAGE_PATH
    )
)
@ApiClass(
    resource = "medias",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class MediaEndpoint {

  /**
   * Returns the {@link Post} with the corresponding ID.
   *
   * @param accountId the ID from the entity to be retrieved
   * @return the entity with the corresponding ID
   * @throws NotFoundException if there is no {@code Feed} with the provided ID.
   */
  @ApiMethod(
      name = "medias.list",
      path = "medias/{accountId}",
      httpMethod = ApiMethod.HttpMethod.POST)
  public CollectionResponse<Media> list(
      @Named("accountId") String accountId,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(accountId, "accountId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(accountId, "Invalid accountId."))
        .validate();

    return getMediaController().listMedias(
        accountId,
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor),
        user);
  }

  private MediaController getMediaController() {
    return MediaController.create(
        MediaService.create());
  }
}
