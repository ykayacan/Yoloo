package com.yoloo.android.data;

import com.annimon.stream.Stream;
import com.yoloo.android.BuildConfig;
import io.reactivex.Single;
import java.io.File;
import java.util.List;
import javax.annotation.Nonnull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public enum UploadManager {
  INSTANCE;

  public Single<Response> upload(@Nonnull String userId, List<File> files, MediaOrigin origin) {
    return Single.fromCallable(() -> {
      final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

      Stream
          .of(files)
          .forEach(file -> builder.addFormDataPart("file", file.getName(),
              RequestBody.create(MediaType.parse("image/webp"), file)));

      final Request request = new Request.Builder()
          .url(BuildConfig.UPLOAD_URL + "?userId=" + userId + "&mediaOrigin=" + origin.name())
          .post(builder.build())
          .build();

      return ApiManager.INSTANCE.getOkHttpClient().newCall(request).execute();
    });
  }

  public enum MediaOrigin {
    POST, PROFILE, CHAT
  }
}
