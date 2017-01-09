package com.simprints.id.tools;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.simprints.id.activities.ALERT_TYPE;

import static com.google.firebase.analytics.FirebaseAnalytics.Event;
import static com.google.firebase.analytics.FirebaseAnalytics.Param;
import static com.google.firebase.analytics.FirebaseAnalytics.UserProperty;

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
        firebaseAnalytics.setUserProperty("api_key", apiKey);
        firebaseAnalytics.setUserProperty(UserProperty.SIGN_UP_METHOD, apiKey);
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

    public void setLogin(boolean enrol) {
        Bundle bundle = new Bundle();
        bundle.putString(Param.VALUE, String.valueOf(enrol));
        firebaseAnalytics.logEvent(Event.LOGIN, bundle);
    }

    public void setBackgroundSync(boolean success, String deviceId) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("success", success);
        bundle.putString("device_id", deviceId);
        firebaseAnalytics.logEvent("background_sync", bundle);
    }
}
