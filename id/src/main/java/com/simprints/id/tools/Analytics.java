package com.simprints.id.tools;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.simprints.id.data.DataManager;

import static com.google.firebase.analytics.FirebaseAnalytics.Event;

@SuppressWarnings("WeakerAccess")
public class Analytics {

    private static Analytics singleton;

    public synchronized static Analytics getInstance(Context context, DataManager dataManager, AppState appState) {
        if (singleton == null) {
            singleton = new Analytics(context, dataManager, appState);
        }
        return singleton;
    }

    private FirebaseAnalytics firebaseAnalytics;
    private AppState appState;
    private DataManager dataManager;

    private Analytics(Context context, DataManager dataManager, AppState appState) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);
        firebaseAnalytics.setMinimumSessionDuration(0);
        this.dataManager = dataManager;
        this.appState = appState;
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAnalytics;
    }

    public void setUserProperties() {
        firebaseAnalytics.setUserId(dataManager.getUserId());
        firebaseAnalytics.setUserProperty("api_key", dataManager.getApiKey());
        firebaseAnalytics.setUserProperty("module_id", dataManager.getModuleId());
        firebaseAnalytics.setUserProperty("user_id", dataManager.getUserId());
        firebaseAnalytics.setUserProperty("device_id", dataManager.getDeviceId());
    }

    public void setScannerMac() {
        firebaseAnalytics.setUserProperty("scanner_id", appState.getMacAddress());
    }


    public void logLogin() {
        Bundle bundle = new Bundle();
        bundle.putString("callout", dataManager.getCallout().name());
        firebaseAnalytics.logEvent(Event.LOGIN, bundle);
    }

//    public void setBackgroundSync(boolean success, String deviceId) {
//        Bundle bundle = new Bundle();
//        bundle.putBoolean("success", success);
//        bundle.putString("device_id", deviceId);
//        firebaseAnalytics.logEvent("background_sync", bundle);
//    }


    public void logGuidSelectionService(String apiKey, String selectedGuid, String androidId,
                                        String sessionId, boolean callbackSent) {
        Bundle bundle = new Bundle();
        bundle.putString("api_key", apiKey);
        bundle.putString("selected_guid", selectedGuid);
        bundle.putString("android_id", androidId);
        bundle.putString("session_id", sessionId);
        bundle.putBoolean("callback_sent", callbackSent);
        firebaseAnalytics.logEvent("guid_selection_service", bundle);
    }
}
