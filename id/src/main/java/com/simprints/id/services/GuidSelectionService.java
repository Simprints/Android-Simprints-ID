package com.simprints.id.services;

import android.app.IntentService;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.simprints.id.Application;
import com.simprints.id.backgroundSync.SyncService;
import com.simprints.id.tools.Analytics;

import static com.simprints.libdata.DatabaseContext.updateIdentification;
import static com.simprints.libsimprints.Constants.SIMPRINTS_API_KEY;
import static com.simprints.libsimprints.Constants.SIMPRINTS_SELECTED_GUID;
import static com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID;


public class GuidSelectionService extends IntentService {

    public GuidSelectionService() {
        super("GuidSelectionService");
    }

    // Singletons
    private SyncService syncService;
    private Analytics analytics;

    @Override
    public void onCreate() {
        super.onCreate();
        Application app = ((Application) getApplication());
        syncService = app.getSyncService();
        analytics = app.getAnalytics();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String apiKey = intent.getStringExtra(SIMPRINTS_API_KEY);
            String sessionId = intent.getStringExtra(SIMPRINTS_SESSION_ID);
            String selectedGuid = intent.getStringExtra(SIMPRINTS_SELECTED_GUID);
            String androidId = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            if (selectedGuid == null) {
                selectedGuid = "null";
            }

            boolean callbackSent;
            if (apiKey == null || androidId == null || sessionId == null) {
                callbackSent = false;
            } else {
                updateIdentification(apiKey, selectedGuid, androidId, sessionId);
                callbackSent = true;
            }
            analytics.logGuidSelectionService(
                    apiKey, selectedGuid, androidId, sessionId, callbackSent);
        }
    }
}
