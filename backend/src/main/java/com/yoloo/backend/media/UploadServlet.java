package com.yoloo.backend.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Strings;
import com.google.common.net.MediaType;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.media.dto.Media;
import com.yoloo.backend.media.transformer.MediaTransformer;
import com.yoloo.backend.util.ServerConfig;
import io.reactivex.Observable;
import ix.Ix;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.ofy;

@Log
public class UploadServlet extends HttpServlet {

  private static final ObjectWriter OW = new ObjectMapper().writer().withDefaultPrettyPrinter();

  private static final String PARAM_USER_ID = "userId";
  private static final String PARAM_MEDIA_ORIGIN = "mediaOrigin";

  private static final MediaTransformer TRANSFORMER = new MediaTransformer();

  private final ImagesService imagesService = ImagesServiceFactory.getImagesService();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setResponse(resp);
    parseRequest(req, resp);
  }

  private void parseRequest(final HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    final PrintWriter out = resp.getWriter();

    final String userId = req.getParameter(PARAM_USER_ID);
    final String mediaOrigin = req.getParameter(PARAM_MEDIA_ORIGIN);

    if (Strings.isNullOrEmpty(userId)) {
      printErrorResponse("userId can not be null.", out);
      return;
    }

    if (Strings.isNullOrEmpty(mediaOrigin)) {
      printErrorResponse("mediaOrigin can not be null.", out);
      return;
    }

    if (!ServletFileUpload.isMultipartContent(req)) {
      printErrorResponse("Request must be multipart/form-data.", out);
      return;
    }

    try {
      Key.<Account>create(userId);
    } catch (Exception e) {
      printErrorResponse("Invalid userId.", out);
      return;
    }

    final Key<Account> accountKey = ofy()
        .load()
        .type(Account.class)
        .filterKey(Key.<Account>create(userId))
        .keys()
        .first()
        .now();

    if (accountKey == null) {
      printErrorResponse("Invalid userId.", out);
      return;
    }

    final ServletFileUpload upload = new ServletFileUpload();

    Storage storage = StorageOptions.getDefaultInstance().getService();

    MediaEntity.MediaOrigin origin = MediaEntity.MediaOrigin.valueOf(mediaOrigin);

    Observable
        .fromCallable(() -> upload.getItemIterator(req))
        .filter(FileItemIterator::hasNext)
        .map(FileItemIterator::next)
        .filter(this::isValidFile)
        .flatMap(file -> {
          final String contentType = file.getContentType();

          /*final ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(
              getServletContext().getResourceAsStream(Constants.FIREBASE_SECRET_JSON_PATH));*/

          /*final Storage storage =
              StorageOptions.newBuilder().setCredentials(credentials).build().getService();*/

          String mediaFileName = MediaUtil.createMediaFileName(contentType);
          String filePath = createMediaPath(origin, accountKey, mediaFileName);

          BucketInfo info = BucketInfo.of(MediaConfig.BASE_BUCKET);
          BlobInfo blobInfo =
              BlobInfo.newBuilder(info, filePath).setContentType(contentType).build();
          Blob blob = storage.create(blobInfo, file.openStream());

          final String servePath =
              MediaConfig.GS_PREFIX + "/" + blob.getBucket() + "/" + blob.getName();

          ServingUrlOptions options =
              ServingUrlOptions.Builder.withGoogleStorageFileName(servePath).secureUrl(true);

          MediaEntity media = createMedia(mediaOrigin, accountKey, contentType, blob, options);

          return Observable.just(media);
        })
        .toList()
        .doOnSuccess(medias -> ofy().transact(() -> ofy().save().entities(medias).now()))
        .map(medias -> Ix.from(medias).map(TRANSFORMER::transformTo).toList())
        .subscribe(mediaDTOS -> printSuccessResponse(mediaDTOS, out),
            throwable -> printErrorResponse(throwable.getMessage(), out));
  }

  private MediaEntity createMedia(String mediaOrigin, Key<Account> accountKey, String contentType,
      Blob blob, ServingUrlOptions options) {
    return MediaEntity
        .builder()
        .parent(accountKey)
        .mime(contentType)
        .url(ServerConfig.isDev() ? "" : imagesService.getServingUrl(options))
        .originalPath(blob.getName())
        .mediaOrigin(MediaEntity.MediaOrigin.parse(mediaOrigin))
        .created(DateTime.now())
        .build();
  }

  private String createMediaPath(MediaEntity.MediaOrigin origin, Key<Account> accountKey,
      String mediaFileName) {

    String path = getBaseMediaPath(origin);

    return Strings.isNullOrEmpty(path)
        ? null
        : path + "/" + accountKey.toWebSafeString() + "/" + mediaFileName;
  }

  private String getBaseMediaPath(MediaEntity.MediaOrigin origin) {
    switch (origin) {
      case POST:
        return MediaConfig.USER_MEDIAS_POSTS_PATH;
      case PROFILE:
        return MediaConfig.USER_MEDIAS_PROFILES_PATH;
      case CHAT:
        return MediaConfig.USER_MEDIAS_CHATS_PATH;
      default:
        return null;
    }
  }

  private boolean isValidFile(FileItemStream file) {
    return MediaType.parse(file.getContentType()).is(MediaType.ANY_IMAGE_TYPE)
        && !file.isFormField()
        && !Strings.isNullOrEmpty(file.getName());
  }

  private void setResponse(HttpServletResponse resp) {
    resp.setContentType("application/json");
    resp.setHeader("Cache-Control", "no-cache");
    resp.setCharacterEncoding("UTF-8");
  }

  private void printSuccessResponse(final Collection<Media> collection, final PrintWriter out)
      throws IOException {
    final String json =
        OW.writeValueAsString(CollectionResponse.<Media>builder().setItems(collection).build());

    out.print(json);
  }

  private void printErrorResponse(final String message, final PrintWriter out) throws IOException {
    final String json = OW.writeValueAsString(WrappedErrorResponse
        .builder()
        .code(HttpServletResponse.SC_BAD_REQUEST)
        .message(message)
        .build());

    out.print(json);
  }
}
