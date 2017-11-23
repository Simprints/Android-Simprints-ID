package com.simprints.id;

import android.content.Context;

import com.simprints.cerberuslibrary.RealmUtility;
import com.simprints.remoteadminclient.Configuration;
import com.simprints.remoteadminclient.api.DefaultApi;
import com.simprints.remoteadminclient.auth.ApiKeyAuth;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.realm.RealmConfiguration;

class Utils {

    static final String PROD_ID = "simprints-152315";

    // TODO: Hide that better
    private static final String API_KEY = "AIzaSyD4j8zfttMqRdAbfxnQ-py-19QqWM--gss";


    static DefaultApi getConfiguredApiInstance() {
        DefaultApi apiInstance = new DefaultApi();
        OkHttpClient okhttpClient = Configuration.getDefaultApiClient().getHttpClient();
        okhttpClient.setConnectTimeout(2, TimeUnit.MINUTES);
        okhttpClient.setReadTimeout(2, TimeUnit.MINUTES);
        okhttpClient.setWriteTimeout(2, TimeUnit.MINUTES);
        okhttpClient.setRetryOnConnectionFailure(true);
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) Configuration.getDefaultApiClient().getAuthentication("ApiKeyAuth");
        apiKeyAuth.setApiKey(API_KEY);
        return apiInstance;
    }

    static void clearApplicationData(Context context, RealmConfiguration realmConfiguration) {
        clearRealmDatabase(realmConfiguration);
        File cacheDirectory = context.getCacheDir();
        if (cacheDirectory == null) return;
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            if (fileNames == null) return;
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    private static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (String child : children) {
                    deletedAll = deleteFile(new File(file, child)) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }
        return deletedAll;
    }

    private static void clearRealmDatabase(RealmConfiguration realmConfiguration) {
        if (realmConfiguration == null) return;
        new RealmUtility().clearRealmDatabase(realmConfiguration);
    }
}
