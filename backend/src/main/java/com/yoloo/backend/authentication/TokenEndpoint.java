package com.yoloo.backend.authentication;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.firebase.FirebaseOptions;

import com.yoloo.backend.Constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

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
        resource = "tokens",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
final class TokenEndpoint {

    private static final Logger logger =
            Logger.getLogger(TokenEndpoint.class.getSimpleName());

    private FirebaseOptions options;

    public TokenEndpoint() {
        try {
            options = new FirebaseOptions.Builder()
                    .setServiceAccount(
                            new FileInputStream("/WEB-INF/yolooapp-server.json"))
                    .setDatabaseUrl("https://yoloo-app.firebaseio.com")
                    .build();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
