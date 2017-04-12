package com.simprints.id.tools;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.model.Callout;

import static com.google.firebase.analytics.FirebaseAnalytics.Event;
import static com.google.firebase.analytics.FirebaseAnalytics.Param;

public class Analytics {

    private static Analytics singleton;

    public synchronized static Analytics getInstance(Context context) {
        if (singleton == null) {
            singleton = new Analytics(context);
        }
        return singleton;
    }

    private FirebaseAnalytics firebaseAnalytics;

    private Analytics(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);
        firebaseAnalytics.setMinimumSessionDuration(0);
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAnalytics;
    }

    public void setUser(String userId, String apiKey) {
        firebaseAnalytics.setUserId(userId);
        firebaseAnalytics.setUserProperty("user_id", userId);
        firebaseAnalytics.setUserProperty("api_key", apiKey);
    }

    public void setDeviceId(String deviceId) {
        firebaseAnalytics.setUserProperty("device_id", deviceId);
    }

    public void setScannerMac(String scannerMac) {
        firebaseAnalytics.setUserProperty("scanner_id", scannerMac);
    }

    public void setAlert(ALERT_TYPE alertType, boolean retry) {
        FirebaseCrash.log(alertType.name());

        Bundle bundle = new Bundle();
        bundle.putString("alert_name", alertType.name());
        bundle.putBoolean("retry", retry);
        firebaseAnalytics.logEvent("alert", bundle);
    }

    public void setLogin(Callout callout) {
        Bundle bundle = new Bundle();
        bundle.putString(Param.VALUE, String.valueOf(callout.name()));
        firebaseAnalytics.logEvent(Event.LOGIN, bundle);
    }

    public void setBackgroundSync(boolean success, String deviceId) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("success", success);
        bundle.putString("device_id", deviceId);
        firebaseAnalytics.logEvent("background_sync", bundle);
    }

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
