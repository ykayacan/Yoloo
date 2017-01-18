package com.yoloo.backend.media;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectMapper;
import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectWriter;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.util.RandomGenerator;
import io.reactivex.Observable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import static com.yoloo.backend.OfyService.ofy;

public class UploadServlet extends HttpServlet {

  private static final Logger logger =
      Logger.getLogger(UploadServlet.class.getName());

  private static final ObjectWriter ow =
      new ObjectMapper().writer().withDefaultPrettyPrinter();

  private static final String PARAM_ACCOUNT_ID = "accountId";

  private static final ServletFileUpload FILE_UPLOAD = new ServletFileUpload();

  private static final ImagesService IMAGES_SERVICE = ImagesServiceFactory.getImagesService();

  private static final Storage.BucketTargetOption BUCKET_OPTION =
      Storage.BucketTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ);

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    setResponse(resp);

    parseRequest(req, resp);
  }

  private void parseRequest(final HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    final String accountId = req.getParameter(PARAM_ACCOUNT_ID);

    final PrintWriter out = resp.getWriter();

    // Check if the request is actually a multipart/form-data request.
    Preconditions.checkArgument(ServletFileUpload.isMultipartContent(req),
        "This isn't a multipart request.");

    final Key<Account> accountKey =
        Preconditions.checkNotNull(ofy().load().type(Account.class)
                .filterKey(Key.<Account>create(accountId)).keys().first().now(),
            "Account id is invalid: %s", accountId);

    Observable
        .create(e -> {
          try {
            FileItemIterator it = FILE_UPLOAD.getItemIterator(req);

            while (it.hasNext()) {
              e.onNext(it.next());
            }
            e.onComplete();
          } catch (FileUploadException t) {
            e.onError(t);
          }
        })
        .cast(FileItemStream.class)
        .filter(this::filterFileStream)
        .map(fileItemStream -> {
          final InputStream is = fileItemStream.openStream();
          final String mime = fileItemStream.getContentType();

          String bucketName = MediaConfig.MEDIA_BUCKET + "/" + accountId;

          Bucket bucket = MediaService.STORAGE.create(BucketInfo.of(bucketName), BUCKET_OPTION);
          Blob blob = bucket.create(RandomGenerator.INSTANCE.generate(), is, mime);

          ServingUrlOptions options = ServingUrlOptions.Builder
              .withGoogleStorageFileName("/gs/" + blob.getBucket() + "/" + blob.getName());

          return Media.builder()
              .id(blob.getBucket() + "/" + blob.getName())
              .parentAccountKey(accountKey)
              .mime(mime)
              .url(IMAGES_SERVICE.getServingUrl(options))
              .build();
        })
        .toList(2)
        .subscribe(medias -> {
          ofy().save().entities(medias).now();

          printSuccessResponse(medias, out);
        });
  }

  private boolean filterFileStream(FileItemStream fileItemStream) {
    return !fileItemStream.isFormField() &&
        !MimeUtil.isValidMime(fileItemStream.getContentType()) &&
        fileItemStream.getName().length() <= 0;
  }

  private void setResponse(HttpServletResponse resp) {
    resp.setContentType("application/json");
    resp.setHeader("Cache-Control", "nocache");
    resp.setCharacterEncoding("UTF-8");
  }

  private void printSuccessResponse(final Collection<Media> collection, final PrintWriter out)
      throws IOException {
    final String json = ow.writeValueAsString(
        WrappedCollectionResponse.<Media>builder()
            .setItems(collection)
            .build());

    out.print(json);
  }

  private void printErrorResponse(final String message, final PrintWriter out)
      throws IOException {
    final String json = ow.writeValueAsString(WrappedErrorResponse.builder()
        .code(HttpServletResponse.SC_BAD_REQUEST)
        .message(message)
        .build());

    out.print(json);
  }
}
