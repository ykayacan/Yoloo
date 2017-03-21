package com.yoloo.android.data;

import com.annimon.stream.Stream;
import com.yoloo.android.BuildConfig;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadManager {

  private static UploadManager instance;

  public static UploadManager getInstance() {
    if (instance == null) {
      instance = new UploadManager();
    }
    return instance;
  }

  public Single<Response> upload(@Nonnull String userId, List<File> files) {
    return Single.fromCallable(() -> {
      final MultipartBody.Builder builder =
          new MultipartBody.Builder().setType(MultipartBody.FORM);

      Stream.of(files).forEach(file -> builder.addFormDataPart("file", file.getName(),
          RequestBody.create(MediaType.parse("image/*"), file)));

      final Request request = new Request.Builder()
          .url(BuildConfig.UPLOAD_URL + "?userId=" + userId)
          .post(builder.build())
          .build();

      return ApiManager.INSTANCE.getOkHttpClient().newCall(request).execute();
    });
  }
}
