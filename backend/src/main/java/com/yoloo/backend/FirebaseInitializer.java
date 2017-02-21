package com.yoloo.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import java.io.InputStream;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class FirebaseInitializer implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final InputStream is = sce.getServletContext()
        .getResourceAsStream(Constants.FIREBASE_SECRET_JSON_PATH);

    FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredential(FirebaseCredentials.fromCertificate(is))
        .setDatabaseUrl(Constants.FIREBASE_APP_URL)
        .build();

    FirebaseApp.initializeApp(options);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

  }
}